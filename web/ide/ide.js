let IDE = (function () {

  let codeEditor;
  let selectedPort = "automatic";
  let blocklyArea, blocklyDiv, workspace;
  let autorunInterval, autorunNextTime;
  let lastProgram;
  let lastFileName;

  let IDE = {
    init: function () {
      initializeLayout();
      initializeTopBar();
      initializeInspectorPanel();
      initializeBlocksPanel();
      initializeCodePanel();
      initializeOutputPanel();
      initializeAutorun();
      initializeServerNotFoundErrorModal();
    }
  };

  function initializeLayout() {
    let config = {
      settings: {
        showPopoutIcon: false,
        showMaximiseIcon: false,
        showCloseIcon: false,
      },
      content: [{
        type: 'row',
        content:[{
          type: 'component',
          componentName: 'ide',
          componentState: { id: '#inspector-panel' },
          title: 'Inspector',
          width: 17,
        },{
          type: 'column',
          content:[{
            type: 'row',
            content: [{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#blocks-panel' },
              title: 'Blocks',
            },{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#code-panel' },
              title: 'Code',
              width: 30,
            }]
          },{
            type: 'stack',
            height: 20,
            content: [{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#output-panel' },
              title: 'Output',
            },{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#test3' },
              title: 'Serial Monitor',
            },{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#test3' },
              title: 'Debugger',
            }]
          }]
        }]
      }]
    };

    let layout = new GoldenLayout(config, "#container");
    layout.registerComponent('ide', function(container, state) {
        let $el = $(state.id);
        container.getElement().append($el);
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
    layout.init();
    updateSize();
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
    Uzi.addObserver(updateTopBar);
    Uzi.addObserver(updateConnection);
  }

  function initializeInspectorPanel() {
    Uzi.addObserver(updateInspectorPanel);
  }

  function initializeBlocksPanel() {
    blocklyArea = $("#blocks-editor").get(0);
    blocklyDiv = $("#blockly").get(0);

    let loadToolbox = ajax.GET('toolbox.xml').then(function (toolbox) {
      workspace = Blockly.inject(blocklyDiv, { toolbox: toolbox });
      workspace.addChangeListener(function () {
        saveToLocalStorage();
        scheduleAutorun(false);
      });
      window.addEventListener('resize', resizeBlockly, false);
      resizeBlockly();
    });

    let loadBlocks = ajax.GET('blocks.json').then(function (json) {
      let blocks = JSON.parse(json);
      Blockly.defineBlocksWithJsonArray(blocks);
    });
    
    Promise.all([loadToolbox, loadBlocks]).then(restoreFromLocalStorage);
  }

  function initializeCodePanel() {
		codeEditor = ace.edit("code-editor");
		codeEditor.setTheme("ace/theme/ambiance");
		codeEditor.getSession().setMode("ace/mode/uzi");
    codeEditor.setReadOnly(true); // TODO(Richo): Only for now...
    Uzi.addObserver(function () {
      let src = Uzi.state.program.current.src;
      if (src == undefined) return;
      if (codeEditor.getValue() !== src) {
        codeEditor.setValue(src, 1);
      }
    });
  }

  function initializeOutputPanel() {
    Uzi.addObserver(function () {
      Uzi.state.output.forEach(function (entry) {
        appendToOutput(entry.text, entry.type);
      });
    });
  }

  function initializeAutorun() {
    setInterval(autorun, 100);
  }

  function initializeServerNotFoundErrorModal() {
    Uzi.addServerDisconnectHandler(function () {
      $("#server-not-found-modal").modal('show');
    });
    setInterval(function () {
      if (Uzi.serverAvailable) {
        $("#server-not-found-modal").modal('hide');
      }
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
    if (blocklyDiv == undefined || blocklyArea == undefined) return;
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
    let ui = {
      blocks: localStorage["uzi.blocks"],
      settings: JSON.parse(localStorage["uzi.settings"] || {}),
    };
    setUIState(ui);
	}

  function saveToLocalStorage() {
    let ui = getUIState();
    localStorage["uzi.blocks"] = ui.blocks;
    localStorage["uzi.settings"] = JSON.stringify(ui.settings);
  }

  function getUIState() {
    return {
      blocks: Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(workspace)),
      settings: {
        interactive: $("#interactive-checkbox").get(0).checked,
      },
    };
  }

  function setUIState(ui) {
    try {
      workspace.clear();
      Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(ui.blocks), workspace);
      $("#interactive-checkbox").get(0).checked = ui.settings.interactive;
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
    Uzi.compile(getGeneratedCodeAsJSON(), "json");
  }

  function run() {
		Uzi.run(getGeneratedCodeAsJSON(), "json");
  }

  function install() {
    Uzi.install(getGeneratedCodeAsJSON(), "json");
  }

  function toggleInteractive() {
    scheduleAutorun($("#interactive-checkbox").get(0).checked);
    saveToLocalStorage();
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
      Uzi.run(currentProgram, "json", true);
    } else {
      Uzi.compile(currentProgram, "json", true);
    }
	}

  function getGeneratedCode(){
    var xml = Blockly.Xml.workspaceToDom(workspace);
    return BlocksToAST.generate(xml);
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

  return IDE;
})();
