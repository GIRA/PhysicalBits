let IDE = (function () {

  let layout, defaultLayoutConfig;
  let codeEditor;
  let selectedPort = "automatic";
  let blocklyArea, blocklyDiv, workspace;
  let autorunInterval, autorunNextTime;
  let lastProgram;
  let lastFileName;
  let motors = [];
  let sonars = [];

  let IDE = {
    init: function () {
      // NOTE(Richo): The following tasks need to be done in order:
      loadDefaultLayoutConfig()
        .then(initializeDefaultLayout)
        .then(initializeBlocksPanel)
        .then(initializeBlocklyMotorsModal)
        .then(initializeBlocklySonarsModal)
        .then(initializeAutorun)
        .then(initializeTopBar)
        .then(initializeInspectorPanel)
        .then(initializeCodePanel)
        .then(initializeOutputPanel)
        .then(initializeBrokenLayoutErrorModal)
        .then(initializeServerNotFoundErrorModal)
        .then(initializeOptionsModal)
        .then(initializeInternationalization);
    }
  };

  function loadDefaultLayoutConfig() {
    return ajax.GET("default-layout.json")
      .then(function (json) { defaultLayoutConfig = JSON.parse(json); });
  }

  function initializeDefaultLayout() {
    initializeLayout(defaultLayoutConfig);
  }

  function initializeLayout(config) {
    if (layout) { layout.destroy(); }
    layout = new GoldenLayout(config, "#layout-container");
    layout.registerComponent('DOM', function(container, state) {
      let $el = $(state.id);
      container.getElement().append($el);
      container.on('destroy', function () {
        $("#hidden-panels").append($el);
      });
    });

    function updateSize() {
      let w = window.innerWidth;
      let h = window.innerHeight - $("#top-bar").outerHeight();
      if (layout.width != w || layout.height != h) {
        layout.updateSize(w, h);
      }
    };

    window.onresize = updateSize;
    layout.on('stateChanged', updateSize);
    layout.on('stateChanged', resizeBlockly);
    layout.on('stateChanged', saveToLocalStorage);
    layout.on('stateChanged', checkBrokenLayout);
    layout.init();
    updateSize();
    resizeBlockly();
    checkBrokenLayout();

    // HACK(Richo): The following allows me to translate panel titles
    $(".lm_title").each(function () { $(this).attr("lang", "en"); });
    i18n.updateUI(); // Force update so that restoring the layout keeps the translated titles
  }

  function initializeTopBar() {
    $("#new-button").on("click", newProject);
    $("#open-button").on("click", openProject);
    $("#save-button").on("click", saveProject);
    $("#port-dropdown").change(choosePort);
    $("#connect-button").on("click", connect);
    $("#disconnect-button").on("click", disconnect);
    $("#verify-button").on("click", verify);
    $("#run-button").on("click", run);
    $("#install-button").on("click", install);
		$("#interactive-checkbox").on("change", toggleInteractive);
    $("#options-button").on("click", openOptionsDialog);
    Uzi.on("update", updateTopBar);
    Uzi.on("update", updateConnection);
  }

  function initializeInspectorPanel() {
    $("#pin-choose-button").on("click", openInspectorPinDialog);
    $("#global-choose-button").on("click", openInspectorGlobalDialog);
    Uzi.on("update", updateInspectorPanel);
  }

  function initializeBlocksPanel() {
    blocklyArea = $("#blocks-editor").get(0);
    blocklyDiv = $("#blockly").get(0);

    let loadToolbox = ajax.GET('toolbox.xml').then(function (toolbox) {
      workspace = Blockly.inject(blocklyDiv, {
      	toolbox: toolbox.documentElement,
       	zoom: {
          controls: true,
          wheel: true,
          startScale: 0.95,
          maxScale: 3,
          minScale: 0.3,
          scaleSpeed: 1.03
        },
      	media: "libs/google-blockly/media/"
      });
      workspace.addChangeListener(function () {
        saveToLocalStorage();
        scheduleAutorun(false);
      });
      workspace.registerToolboxCategoryCallback("TASKS", function () {
        let node = XML.getChildNode(toolbox.documentElement, "Tasks");
        let nodes = Array.from(node.children);
        let tasks = getCurrentTaskNames();
        if (tasks.length > 0) {

          let blocks = Array.from(node.getElementsByTagName("block"))
            .filter(function (block) {
              switch (block.getAttribute("type")) {
                case "start_task":
                case "stop_task":
                case "resume_task":
                case "pause_task":
                case "run_task":
                  return true;
                default:
                  return false;
              }
            });

          let fields = blocks.map(function (block) {
            return Array.from(block.getElementsByTagName("field"))
              .filter(function (field) { return field.getAttribute("name") == "taskName"; });
          }).flat();

          fields.forEach(function (field) {
            field.innerText = tasks[tasks.length-1];
          });
        }
        return nodes;
      });
      workspace.registerToolboxCategoryCallback("DC_MOTORS", function () {
        let node = XML.getChildNode(XML.getChildNode(toolbox.documentElement, "Motors"), "DC");
        let nodes = Array.from(node.children);
        if (motors.length == 0) {
          nodes.splice(1); // Leave the button only
        } else {
          let fields = node.getElementsByTagName("field");
          for (let i = 0; i < fields.length; i++) {
            let field = fields[i];
            if (field.getAttribute("name") === "motorName") {
              field.innerText = motors[motors.length-1].name;
            }
          }
        }
        return nodes;
      });
      workspace.registerToolboxCategoryCallback("SONAR", function () {
        let node = XML.getChildNode(XML.getChildNode(toolbox.documentElement, "Sensors"), "Sonar");
        let nodes = Array.from(node.children);
        if (sonars.length == 0) {
          nodes.splice(1); // Leave the button only
        } else {
          let fields = node.getElementsByTagName("field");
          for (let i = 0; i < fields.length; i++) {
            let field = fields[i];
            if (field.getAttribute("name") === "sonarName") {
              field.innerText = sonars[sonars.length-1].name;
            }
          }
        }
        return nodes;
      });
      window.addEventListener('resize', resizeBlockly, false);
      resizeBlockly();
    });

    let loadBlocks = ajax.GET('blocks.json').then(function (json) {
      let blocks = JSON.parse(json);
      Blockly.defineBlocksWithJsonArray(blocks);
      initSpecialBlocks();
    });

    return Promise.all([loadToolbox, loadBlocks]).then(restoreFromLocalStorage);
  }

  function initSpecialBlocks() {
    initTaskBlocks();
    initDCMotorBlocks();
    initSonarBlocks();
  }

  function getCurrentTaskNames() {
    let program = Uzi.state.program.current;
    if (program == null) return [];

    // HACK(Richo): Filtering by the class name...
    return program.ast.scripts
      .filter(function (s) { return s.__class__ == "UziTaskNode"; })
      .map(function (each) { return each.name; });
  }

  function initTaskBlocks() {
    function currentTasksForDropdown() {
      let tasks = getCurrentTaskNames();
      if (tasks.length == 0) return [["", ""]];
      return tasks.map(function (name) { return [ name, name ]; });
    }

    let blocks = [
      ["start_task", "start"],
      ["stop_task", "stop"],
      ["run_task", "run"],
      ["resume_task", "resume"],
      ["pause_task", "pause"],
    ];

    blocks.forEach(function (block) {
      Blockly.Blocks[block[0]] = {
        init: function() {
          this.appendDummyInput()
              .appendField(block[1])
              .appendField(new Blockly.FieldDropdown(currentTasksForDropdown), "taskName");
          this.setPreviousStatement(true, null);
          this.setNextStatement(true, null);
          this.setColour(175);
         this.setTooltip("");
         this.setHelpUrl("");
        }
      };
    });
  }

  function initDCMotorBlocks() {
    function currentMotorsForDropdown() {
      if (motors.length == 0) return [["", ""]];
      return motors.map(function(each) { return [ each.name, each.name ]; });
    }

    Blockly.Blocks['move_dcmotor'] = {
      init: function() {
        this.appendValueInput("speed")
            .setCheck("Number")
            .appendField("move")
            .appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName")
            .appendField(new Blockly.FieldDropdown([["forward","fwd"], ["backward","bwd"]]), "direction")
            .appendField("at speed");
        //this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['stop_dcmotor'] = {
      init: function() {
        this.appendDummyInput()
            .appendField("stop")
            .appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName");
        //this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['change_speed_dcmotor'] = {
      init: function() {
        this.appendValueInput("speed")
            .setCheck("Number")
            .appendField("set")
            .appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName")
            .appendField("speed to");
        //this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };
  }

  function initSonarBlocks() {
      function currentSonarsForDropdown() {
        if (sonars.length == 0) return [["", ""]];
        return sonars.map(function(each) { return [ each.name, each.name ]; });
      }

      Blockly.Blocks['get_sonar_distance'] = {
        init: function() {
          this.appendDummyInput()
              .appendField("read distance from")
              .appendField(new Blockly.FieldDropdown(currentSonarsForDropdown), "sonarName")
              .appendField("in")
              .appendField(new Blockly.FieldDropdown([["mm","mm"], ["cm","cm"], ["m","m"]]), "unit");
          this.setInputsInline(true);
          this.setOutput(true, "Number");
          this.setColour(0);
          this.setTooltip("");
          this.setHelpUrl("");
        }
      };
  }

  function initializeBlocklyMotorsModal() {
    let count = 0;

    function getUsedMotors() {
      let program = Uzi.state.program.current;
      if (program == null) return new Set();
      // HACK(Richo): We are actually returning all the aliases, not just motors
      return new Set(program.ast.imports.map(imp => imp.alias));
    }

    function getDefaultMotor() {
      let data = $("#blockly-motors-modal-container").serializeJSON();
      let motorNames = new Set();
      for (let i in data.motors) {
        motorNames.add(data.motors[i].name);
      }
      let motor = { name: "motor", enable: "D10", fwd: "D9", bwd: "D8" };
      let i = 1;
      while (motorNames.has(motor.name)) {
        motor.name = "motor" + i;
        i++;
      }
      return motor;
    }

    function appendMotorRow(motor, usedMotors) {
      let i = count++;

      function createTextInput(motorName, controlName) {
        let input = $("<input>")
          .attr("type", "text")
          .addClass("form-control")
          .addClass("text-center")
          .attr("name", controlName);
        input.get(0).value = motorName;
        return input;
      }
      function createPinDropdown(motorPin, motorName) {
        let select = $("<select>")
          .addClass("form-control")
          .attr("name", motorName);
        Uzi.state.pins.available.forEach(function (pin) {
          select.append($("<option>").text(pin.name));
        });
        select.get(0).value = motorPin;
        return select;
      }
      function createRemoveButton(row) {
        var btn = $("<button>")
          .addClass("btn")
          .addClass("btn-sm")
          .attr("type", "button")
          .append($("<i>")
            .addClass("fas")
            .addClass("fa-minus"));

        if (usedMotors.has(motor.name)) {
          btn
            //.attr("disabled", "true")
            .addClass("btn-outline-secondary")
            .attr("data-toggle", "tooltip")
            .attr("data-placement", "left")
            .attr("title", "This motor is being used by the program!")
            .on("click", function () {
              btn.tooltip("toggle");
            });
        } else {
          btn
            .addClass("btn-outline-danger")
            .on("click", function () { row.remove(); });
        }
        return btn;
      }
      let tr = $("<tr>")
        .append($("<td>").append(createTextInput(motor.name, "motors[" + i + "][name]")))
        .append($("<td>").append(createPinDropdown(motor.enable, "motors[" + i + "][enable]")))
        .append($("<td>").append(createPinDropdown(motor.fwd, "motors[" + i + "][fwd]")))
        .append($("<td>").append(createPinDropdown(motor.bwd, "motors[" + i + "][bwd]")))
      tr.append($("<td>").append(createRemoveButton(tr)));
      $("#blockly-motors-modal-container-tbody").append(tr);
    }

    $("#add-motor-row-button").on("click", function () {
      appendMotorRow(getDefaultMotor(), getUsedMotors());
    });

    workspace.registerButtonCallback("configureDCMotors", function () {
      // Build modal UI
      $("#blockly-motors-modal-container-tbody").html("");
      let usedMotors = getUsedMotors();
      if (motors.length == 0) {
        appendMotorRow(getDefaultMotor(), usedMotors);
      }
      motors.forEach(function (motor) {
        appendMotorRow(motor, usedMotors);
      });
      $("#blockly-motors-modal").modal("show");
    });

    $("#blockly-motors-modal").on("hide.bs.modal", function () {
      let data = $("#blockly-motors-modal-container").serializeJSON();
      let temp = [];
      for (let i in data.motors) {
        temp.push(data.motors[i]);
      }
      // TODO(Richo): Check program and rename/disable motor blocks accordingly
      motors = temp;
      workspace.toolbox_.refreshSelection();
      saveToLocalStorage();
    });

    $("#blockly-motors-modal-container").on("submit", function (e) {
      e.preventDefault();
      $("#blockly-motors-modal").modal("hide");
    });
  }

  function initializeBlocklySonarsModal() {
    let count = 0;

    function getUsedSonars() {
      let program = Uzi.state.program.current;
      if (program == null) return new Set();
      // HACK(Richo): We are actually returning all the aliases, not just sonars
      return new Set(program.ast.imports.map(imp => imp.alias));
    }

    function getDefaultSonar() {
      let data = $("#blockly-sonars-modal-container").serializeJSON();
      let sonarNames = new Set();
      for (let i in data.sonars) {
        sonarNames.add(data.sonars[i].name);
      }
      let sonar = { name: "sonar", trig: "D11", echo: "D12", maxDist: "200" };
      let i = 1;
      while (sonarNames.has(sonar.name)) {
        sonar.name = "sonar" + i;
        i++;
      }
      return sonar;
    }

    function appendSonarRow(sonar, usedSonars) {
      let i = count++;

      function createTextInput(controlValue, controlName) {
        let input = $("<input>")
          .attr("type", "text")
          .addClass("form-control")
          .addClass("text-center")
          .attr("name", controlName);
        input.get(0).value = controlValue;
        return input;
      }
      function createPinDropdown(sonarPin, sonarName) {
        let select = $("<select>")
          .addClass("form-control")
          .attr("name", sonarName);
        Uzi.state.pins.available.forEach(function (pin) {
          select.append($("<option>").text(pin.name));
        });
        select.get(0).value = sonarPin;
        return select;
      }
      function createRemoveButton(row) {
        var btn = $("<button>")
          .addClass("btn")
          .addClass("btn-sm")
          .attr("type", "button")
          .append($("<i>")
            .addClass("fas")
            .addClass("fa-minus"));

        if (usedSonars.has(sonar.name)) {
          btn
            //.attr("disabled", "true")
            .addClass("btn-outline-secondary")
            .attr("data-toggle", "tooltip")
            .attr("data-placement", "left")
            .attr("title", "This sonar is being used by the program!")
            .on("click", function () {
              btn.tooltip("toggle");
            });
        } else {
          btn
            .addClass("btn-outline-danger")
            .on("click", function () { row.remove(); });
        }
        return btn;
      }
      let tr = $("<tr>")
        .append($("<td>").append(createTextInput(sonar.name, "sonars[" + i + "][name]")))
        .append($("<td>").append(createPinDropdown(sonar.trig, "sonars[" + i + "][trig]")))
        .append($("<td>").append(createPinDropdown(sonar.echo, "sonars[" + i + "][echo]")))
        .append($("<td>").append(createTextInput(sonar.maxDist, "sonars[" + i + "][maxDist]")))
      tr.append($("<td>").append(createRemoveButton(tr)));
      $("#blockly-sonars-modal-container-tbody").append(tr);
    }

    $("#add-sonar-row-button").on("click", function () {
      appendSonarRow(getDefaultSonar(), getUsedSonars());
    });

    workspace.registerButtonCallback("configureSonars", function () {
      // Build modal UI
      $("#blockly-sonars-modal-container-tbody").html("");
      let usedSonars = getUsedSonars();
      if (sonars.length == 0) {
        appendSonarRow(getDefaultSonar(), usedSonars);
      }
      sonars.forEach(function (sonar) {
        appendSonarRow(sonar, usedSonars);
      });
      $("#blockly-sonars-modal").modal("show");
    });

    $("#blockly-sonars-modal").on("hide.bs.modal", function () {
      let data = $("#blockly-sonars-modal-container").serializeJSON();
      let temp = [];
      for (let i in data.sonars) {
        temp.push(data.sonars[i]);
      }
      // TODO(Richo): Check program and rename/disable sonar blocks accordingly
      sonars = temp;
      workspace.toolbox_.refreshSelection();
      saveToLocalStorage();
    });

    $("#blockly-sonars-modal-container").on("submit", function (e) {
      e.preventDefault();
      $("#blockly-sonars-modal").modal("hide");
    });
  }

  function initializeCodePanel() {
		codeEditor = ace.edit("code-editor");
		codeEditor.setTheme("ace/theme/ambiance");
		codeEditor.getSession().setMode("ace/mode/uzi");
    codeEditor.setReadOnly(true); // TODO(Richo): Only for now...
    Uzi.on("update", function () {
      let src = Uzi.state.program.current.src;
      if (src == undefined) return;
      if (codeEditor.getValue() !== src) {
        codeEditor.setValue(src, 1);
      }
    });
  }

  function initializeOutputPanel() {
    Uzi.on("update", function () {
      Uzi.state.output.forEach(function (entry) {
        appendToOutput(entry.text, entry.type);
      });
    });
  }

  function initializeAutorun() {
    setInterval(autorun, 100);
  }

  function initializeInternationalization() {
    i18n.init(TRANSLATIONS);
    i18n.currentLocale("es");
    $("#spinner-container").hide();
  }

  function initializeBrokenLayoutErrorModal() {
    $("#fix-broken-layout-button").on("click", function () {
      initializeDefaultLayout();
      $("#broken-layout-modal").modal("hide");
    });
  }

  function initializeServerNotFoundErrorModal() {
    Uzi.on("server-disconnect", function () {
      $("#server-not-found-modal").modal('show');
    });
    setInterval(function () {
      if (Uzi.serverAvailable) {
        $("#server-not-found-modal").modal('hide');
      }
    }, 1000);
  }

  function initializeOptionsModal() {
    $("#restore-layout-button").on("click", initializeDefaultLayout);

    $('input[name="language-radios"]:radio').change(function () {
      i18n.currentLocale(this.value);
    })
    i18n.on("change", function () {
      let locale = i18n.currentLocale();
      if (locale.startsWith("en")) {
        $("#english-radio").prop("checked", true);
      }
      if (locale.startsWith("es")) {
        $("#spanish-radio").prop("checked", true);
      }
      console.log(locale);
    });
  }

  function checkBrokenLayout() {
    if (layout.config.content.length > 0) return;

    setTimeout(function () {
      if (layout.config.content.length > 0) return;
      $("#broken-layout-modal").modal("show");
    }, 1000);
  }

  function appendToOutput(text, type) {
    let css = {
      info: "text-white",
      success: "text-success",
      error: "text-danger",
      warning: "text-warning"
    };

    let entry = $("<div>")
      .addClass("small")
      .addClass(css[type]);
    if (text) { entry.text(text); }
    else { entry.html("&nbsp;"); }

    $("#output-console").append(entry);

    // Scroll to bottom
    let panel = $("#output-panel").get(0);
    panel.scrollTop = panel.scrollHeight - panel.clientHeight;
  }

  function resizeBlockly() {
    // Only if Blockly was initialized
    if (workspace == undefined) return;

    let x, y;
    x = y = 0;
    blocklyDiv.style.left = x + 'px';
    blocklyDiv.style.top = y + 'px';
    let scale = 1/0.85;
    blocklyDiv.style.width = (blocklyArea.offsetWidth * scale) + 'px';
    blocklyDiv.style.height = (blocklyArea.offsetHeight * scale) + 'px';
    Blockly.svgResize(workspace);
  }

	function restoreFromLocalStorage() {
    try {
      let ui = {
        blocks: localStorage["uzi.blocks"],
        settings: JSON.parse(localStorage["uzi.settings"] || {}),
        layout: JSON.parse(localStorage["uzi.layout"] || "null"),
        motors: JSON.parse(localStorage["uzi.motors"]),
        sonars: JSON.parse(localStorage["uzi.sonars"]),
      };
      setUIState(ui);
    } catch (err) {
      console.log(err);
    }
	}

  function saveToLocalStorage() {
    if (workspace == undefined || layout == undefined) return;

    let ui = getUIState();
    localStorage["uzi.blocks"] = ui.blocks;
    localStorage["uzi.settings"] = JSON.stringify(ui.settings);
    localStorage["uzi.layout"] = JSON.stringify(ui.layout);
    localStorage["uzi.motors"] = JSON.stringify(ui.motors);
    localStorage["uzi.sonars"] = JSON.stringify(ui.sonars);
  }

  function getUIState() {
    return {
      blocks: Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(workspace)),
      settings: {
        interactive: $("#interactive-checkbox").get(0).checked,
      },
      layout: layout.toConfig(),
      motors: motors,
      sonars: sonars,
    };
  }

  function setUIState(ui) {
    try {
      if (ui.layout) {
        initializeLayout(ui.layout);
      }

      if (ui.blocks) {
        workspace.clear();
        Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(ui.blocks), workspace);
      }

      if (ui.settings) {
        $("#interactive-checkbox").get(0).checked = ui.settings.interactive;
      }

      if (ui.motors) {
        motors = ui.motors;
      }

      if (ui.sonars) {
        sonars = ui.sonars;
      }
    } catch (err) {
      console.error(err);
    }
  }

  function newProject() {
		if (confirm("You will lose all your unsaved changes. Are you sure?")) {
			workspace.clear();
		}
  }

  function openProject() {
    let input = $("#open-file-input").get(0);
    input.onchange = function () {
      let file = input.files[0];
      input.value = null;
      if (file === undefined) return;

      let reader = new FileReader();
      reader.onload = function(e) {
        try {
          let json = e.target.result;
          let ui = JSON.parse(json);
          setUIState(ui);
        } catch (err) {
          console.log(err);
          appendToOutput("Error attempting to read the project file", "error");
        }
      };
      reader.readAsText(file);
    };
    input.click();
  }

  function saveProject() {
    lastFileName = prompt("File name:", lastFileName || "program.phb");
    if (lastFileName === null) return;
    if (!lastFileName.endsWith(".phb")) {
      lastFileName += ".phb";
    }
    try {
      let ui = getUIState();
      let json = JSON.stringify(ui);
      var blob = new Blob([json], {type: "text/plain;charset=utf-8"});
      saveAs(blob, lastFileName);
    } catch (err) {
      console.log(err);
      appendToOutput("Error attempting to write the project file", "error");
    }
  }

  function choosePort() {
    let value = $("#port-dropdown").val();
    if (value == "other") {
      let defaultOption = selectedPort == "automatic" ? "" : selectedPort;
      value = prompt("Port name:", defaultOption);
      if (!value) { value = selectedPort; }
    }
    setSelectedPort(value);
  }

  function setSelectedPort(val) {
    selectedPort = val;
    if ($("#port-dropdown option[value='" + selectedPort + "']").length <= 0) {
      $("<option>")
        .text(selectedPort)
        .attr("value", selectedPort)
        .insertBefore("#port-dropdown-divider");
    }
    $("#port-dropdown").val(selectedPort);
  }

  function connect() {
    $("#connect-button").attr("disabled", "disabled");
    Uzi.connect(selectedPort);
  }

  function disconnect() {
    $("#disconnect-button").attr("disabled", "disabled");
    Uzi.disconnect();
  }

  function verify() {
    Uzi.compile(getGeneratedCodeAsJSON(), "json").then(success).catch(error);
  }

  function run() {
		Uzi.run(getGeneratedCodeAsJSON(), "json").then(success).catch(error);
  }

  function install() {
    Uzi.install(getGeneratedCodeAsJSON(), "json");
  }

  function toggleInteractive() {
    scheduleAutorun($("#interactive-checkbox").get(0).checked);
    saveToLocalStorage();
  }

  function openOptionsDialog() {
    $("#options-modal").modal("show");
  }

  function openInspectorPinDialog() {
    buildPinInspectorDialog();
    $("#inspector-pin-modal").modal("show");
  }

  function openInspectorGlobalDialog() {
    buildGlobalInspectorDialog();
    $("#inspector-global-modal").modal("show");
  }

  function updateConnection (newState, previousState) {
    if (previousState == null
        || (!previousState.isConnected && newState.isConnected)) {
      scheduleAutorun(true);
    }
  }

	function scheduleAutorun(forced) {
		var currentTime = +new Date();
		autorunNextTime = currentTime;
    if (forced) { lastProgram = null; }
	}

  function success() {
    $(document.body).css("border", "4px solid black");
  }

  function error() {
    $(document.body).css("border", "4px solid red");
  }

	function autorun() {
		if (autorunNextTime === undefined) return;

		let currentTime = +new Date();
		if (currentTime < autorunNextTime) return;

		let currentProgram = getGeneratedCodeAsJSON();
		if (currentProgram === lastProgram) return;

		autorunNextTime = undefined;
    lastProgram = currentProgram;

    let interactiveEnabled = $("#interactive-checkbox").get(0).checked;
    if (Uzi.state.isConnected && interactiveEnabled) {
      Uzi.run(currentProgram, "json", true).then(success).catch(error);
    } else {
      Uzi.compile(currentProgram, "json", true).then(success).catch(error);
    }
	}

  function getGeneratedCode(){
    var xml = Blockly.Xml.workspaceToDom(workspace);
    return BlocksToAST.generate(xml, motors, sonars);
  }

  function getGeneratedCodeAsJSON() {
    var code = getGeneratedCode();
    return JSON.stringify(code);
  }

  function updateTopBar() {
    if (Uzi.state.isConnected) {
      $("#connect-button").hide();
      $("#disconnect-button").show();
      $("#disconnect-button").attr("disabled", null);
      $("#port-dropdown").attr("disabled", "disabled");
      $("#run-button").attr("disabled", null);
      $("#install-button").attr("disabled", null);
      setSelectedPort(Uzi.state.portName);
    } else {
      $("#disconnect-button").hide();
      $("#connect-button").show();
      $("#connect-button").attr("disabled", null);
      $("#port-dropdown").attr("disabled", null);
      $("#run-button").attr("disabled", "disabled");
      $("#install-button").attr("disabled", "disabled");
    }
  }

  function updateInspectorPanel() {
    updatePinsPanel();
    updateGlobalsPanel();
    updateTasksPanel();
  }

  function updatePinsPanel() {
    updateValuesPanel(Uzi.state.pins, $("#pins-table tbody"), $("#no-pins-label"), "pin");
  }

  function updateGlobalsPanel() {
    updateValuesPanel(Uzi.state.globals, $("#globals-table tbody"), $("#no-globals-label"), "global");
  }

  function updateValuesPanel(values, $container, $emptyLabel, itemPrefix) {
    let reporting = new Set();
    values.available.forEach(function (val) {
      if (val.reporting) { reporting.add(val.name); }
    });

    if (reporting.size == 0) {
      $emptyLabel.show();
      $container.html("");
      return;
    } else {
      $emptyLabel.hide();
    }

    function getElementId(val) { return itemPrefix + "-" + val.name; }

    // NOTE(Richo): The value could have ".", which bothers JQuery but works with document.getElementById
    function getElement(val) { return $(document.getElementById(getElementId(val))); }

    function initializePanel() {
      $container.html("");
      values.available.forEach(function (val) {
        if (val.reporting) {
          let $row = $("<tr>")
            .append($("<td>")
              .addClass("pl-4")
              .html('<i class="fas fa-eye"></i>'))
            .append($("<td>")
              .text(val.name))
            .append($("<td>")
              .addClass("text-right")
              .addClass("pr-4")
              .addClass("text-muted")
              .attr("id", getElementId(val))
              .text("?"));
          $container.append($row);
        }
      });
    };

    if (values.available
        .filter(function (val) { return val.reporting; })
        .some(function (val) {
          let $item = getElement(val);
          return $item.get(0) == undefined;
        })) {
      initializePanel();
    }

    values.elements.forEach(function (val) {
      if (reporting.has(val.name)) {
        let $item = getElement(val);
        if ($item.get(0) == undefined) { initializePanel(); }

        let old = $item.data("old-value");
        let cur = val.value;
        if (cur != null && cur != old && Uzi.state.isConnected) {
          $item.data("old-value", cur);
          $item.data("last-update", +new Date());
          $item.removeClass("text-muted");
        } else {
          let lastUpdate = $item.data("last-update") || 0;
          let now = +new Date();
          if (now - lastUpdate > 2500) {
            $item.addClass("text-muted");
          }
        }

        if (cur != null) {
          if (cur == Infinity) {
            $item.text("∞");
          } else if (cur == -Infinity) {
            $item.text("-∞");
          } else if (isNaN(cur)) {
            $item.text("NaN");
          } else {
            $item.text(cur.toFixed(2));
          }
        } else {
          $item.text("?");
        }
      }
    });

    values.available.forEach(function (val) {
      if (!reporting.has(val.name)) {
        let $item = getElement(val);
        if ($item != undefined) { $item.parent().remove(); }
      }
    });
  }

  function updateTasksPanel() {
    // TODO(Richo): Update in place, don't clear and recreate.
    $("#tasks-table tbody").html("");
    if (!Uzi.state.isConnected) return;

    for (let i = 0; i < Uzi.state.tasks.length; i++) {
      let task = Uzi.state.tasks[i];
      let css = "text-muted";
      let html = "";
      if (task.isError) {
        css = "text-warning";
        html = '<i class="fas fa-skull-crossbones mr-2"></i>error';
      } else if (task.isRunning) {
        css = "text-success";
        html = '<i class="fas fa-running mr-2"></i>running';
      } else {
        css = "text-danger";
        html = '<i class="fas fa-hand-paper mr-2"></i>stopped';
      }
      $("#tasks-table tbody")
        .append($("<tr>")
          .append($("<td>")
            .addClass("pl-4")
            .text(task.scriptName))
          .append($("<td>")
            .addClass(css)
            .html(html)));
    }
  }

  function buildPinInspectorDialog() {
    let container = $("#inspector-pin-modal-container");
    container.html("");

    let ncols = 6;
    let row;

    function buildInput (pin, index) {
      if (index % ncols == 0) {
        row = $("<div>").addClass("row");
        container.append(row);
      }
      let id = pin.name + "-checkbox";
      let input = $("<input>")
        .attr("type", "checkbox")
        .attr("id", id)
        .attr("name", "pins-checkbox")
        .attr("value", pin.name)
        .addClass("custom-control-input");
      input.get(0).checked = pin.reporting;
      input.on("change", function () {
        let reportEnabled = this.checked;
        Uzi.setPinReport([pin.name], [reportEnabled]);
      });

      row.append($("<div>")
        .addClass("col-" + (12 / ncols))
        .append($("<div>")
          .addClass("custom-control")
          .addClass("custom-checkbox")
          .addClass("custom-control-inline")
          .append(input)
          .append($("<label>")
            .addClass("custom-control-label")
            .attr("for", id)
            .text(pin.name))));
    }

    // Digital pins
    container.append($("<h6>").text("Digital:"));
    Uzi.state.pins.available
      .filter(function (pin) { return pin.name.startsWith("D"); })
      .forEach(buildInput);

    // Analog pins
    container.append($("<h6>").addClass("mt-4").text("Analog:"));
    Uzi.state.pins.available
      .filter(function (pin) { return pin.name.startsWith("A"); })
      .forEach(buildInput);
  }

  function buildGlobalInspectorDialog() {
    let container = $("#inspector-global-modal-container");
    container.html("");

    let ncols = 6;
    let row;

    function buildInput (global, index) {
      if (index % ncols == 0) {
        row = $("<div>").addClass("row");
        container.append(row);
      }
      let id = "global-" + global.name + "-checkbox";
      let input = $("<input>")
        .attr("type", "checkbox")
        .attr("id", id)
        .attr("name", "globals-checkbox")
        .attr("value", global.name)
        .addClass("custom-control-input");
      input.get(0).checked = global.reporting;
      input.on("change", function () {
        let reportEnabled = this.checked;
        Uzi.setGlobalReport([global.name], [reportEnabled]);
      });

      row.append($("<div>")
        .addClass("col-" + (12 / ncols))
        .append($("<div>")
          .addClass("custom-control")
          .addClass("custom-checkbox")
          .addClass("custom-control-inline")
          .append(input)
          .append($("<label>")
            .addClass("custom-control-label")
            .attr("for", id)
            .text(global.name))));
    }

    container.append($("<h6>").text("Global:"));
    let globals = Uzi.state.globals.available;
    if (globals.length == 0) {
      container.append($("<i>").text("* No globals found *"));
    } else {
      Uzi.state.globals.available.forEach(buildInput);
    }
  }

  return IDE;
})();
