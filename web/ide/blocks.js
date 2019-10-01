let UziBlock = (function () {

  let blocklyArea, blocklyDiv, workspace;
  let motors = [];
  let sonars = [];
  let observers = {
    "change" : [],
  };

  function init() {
    blocklyArea = $("#blocks-editor").get(0);
    blocklyDiv = $("#blockly").get(0);

    initCommonBlocks();
    initSpecialBlocks();

    i18n.on("change", refreshWorkspace);

    return ajax.GET('toolbox.xml').then(function (toolbox) {
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
        trigger("change");
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
  }


  function initCommonBlocks() {

    Blockly.Blocks['toggle_variable'] = {
      init: function() {
        this.appendValueInput("pinNumber")
        .setCheck("Number")
        .appendField("toggle pin");
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['wait'] = {
      init: function() {
        this.appendValueInput("condition")
        .setCheck("Boolean")
        .appendField("wait")
        .appendField(new Blockly.FieldDropdown([["while","false"], ["until","true"]]), "negate");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['turn_pin_variable'] = {
      init: function() {
        this.appendValueInput("pinNumber")
        .setCheck("Number")
        .appendField("turn")
        .appendField(new Blockly.FieldDropdown([["on","on"], ["off","off"]]), "pinState")
        .appendField("pin");
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['is_pin_variable'] = {
      init: function() {
        this.appendValueInput("pinNumber")
        .setCheck("Number")
        .appendField("is")
        .appendField(new Blockly.FieldDropdown([["on","on"], ["off","off"]]), "pinState")
        .appendField("pin");
        this.setInputsInline(true);
        this.setOutput(true, "Boolean");
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['read_pin_variable'] = {
      init: function() {
        this.appendValueInput("pinNumber")
        .setCheck("Number")
        .appendField("read pin");
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['write_pin_variable'] = {
      init: function() {
        this.appendValueInput("pinNumber")
        .setCheck("Number")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("write pin");
        this.appendValueInput("pinValue")
        .setCheck("Number")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("value");
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['start_task'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("start")
        .appendField(new Blockly.FieldTextInput("task name"), "taskName");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['stop_task'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("stop")
        .appendField(new Blockly.FieldTextInput("task name"), "taskName");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['run_task'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("run")
        .appendField(new Blockly.FieldTextInput("task name"), "taskName");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['resume_task'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("resume")
        .appendField(new Blockly.FieldTextInput("task name"), "taskName");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['pause_task'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("pause")
        .appendField(new Blockly.FieldTextInput("task name"), "taskName");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['forever'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("repeat forever");
        this.appendStatementInput("statements")
        .setCheck(null)
        .appendField("do");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['delay'] = {
      init: function() {
        this.appendValueInput("time")
        .setCheck("Number")
        .appendField("wait");
        this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown([["milliseconds","ms"], ["seconds","s"], ["minutes","m"]]), "unit");
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['comment_statement'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("\"")
        .appendField(new Blockly.FieldTextInput("This is a comment"), "comment")
        .appendField("\"");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(20);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['comment_expression'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("\"")
        .appendField(new Blockly.FieldTextInput("This is a comment"), "comment")
        .appendField("\"");
        this.appendValueInput("NAME")
        .setCheck(null)
        .setAlign(Blockly.ALIGN_CENTRE);
        this.setInputsInline(true);
        this.setOutput(true, null);
        this.setColour(20);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['elapsed_time'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("elapsed")
        .appendField(new Blockly.FieldDropdown([["milliseconds","ms"], ["seconds","s"], ["minutes","m"]]), "unit");
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['degrees_servo_variable'] = {
      init: function() {
        this.appendValueInput("pinNumber")
        .setCheck("Number")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("move servo on pin");
        this.appendValueInput("servoValue")
        .setCheck("Number")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("degrees");
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['repeat'] = {
      init: function() {
        this.appendValueInput("condition")
        .setCheck("Boolean")
        .appendField("repeat")
        .appendField(new Blockly.FieldDropdown([["while","false"], ["until","true"]]), "negate");
        this.appendStatementInput("statements")
        .setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['repeat_times'] = {
      init: function() {
        this.appendValueInput("times")
        .setCheck(null)
        .appendField("repeat");
        this.appendDummyInput()
        .appendField("times");
        this.appendStatementInput("statements")
        .setCheck(null)
        .appendField("do");
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['for'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("count with")
        .appendField(new Blockly.FieldVariable("i"), "variable");
        this.appendValueInput("start")
        .setCheck("Number")
        .appendField("from");
        this.appendValueInput("stop")
        .setCheck("Number")
        .appendField("to");
        this.appendValueInput("step")
        .setCheck("Number")
        .appendField("by");
        this.appendStatementInput("statements")
        .setCheck(null)
        .appendField("do");
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['timer'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("timer named")
        .appendField(new Blockly.FieldTextInput("default"), "taskName");
        this.appendDummyInput()
        .appendField("running")
        .appendField(new Blockly.FieldNumber(1000, 0, 1999), "runningTimes")
        .appendField("times per")
        .appendField(new Blockly.FieldDropdown([["second","s"], ["minute","m"], ["hour","h"]]), "tickingScale");
        this.appendDummyInput()
        .appendField("initial state")
        .appendField(new Blockly.FieldDropdown([["started","started"], ["stopped","stopped"]]), "initialState");
        this.appendStatementInput("statements")
        .setCheck(null)
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("do");
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['task'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("task named")
        .appendField(new Blockly.FieldTextInput("default"), "taskName");
        this.appendStatementInput("statements")
        .setCheck(null)
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("do");
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['conditional_simple'] = {
      init: function() {
        this.appendValueInput("condition")
        .setCheck("Boolean")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("if");
        this.appendStatementInput("trueBranch")
        .setCheck(null)
        .appendField("do");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['conditional_full'] = {
      init: function() {
        this.appendValueInput("condition")
        .setCheck("Boolean")
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("if");
        this.appendStatementInput("trueBranch")
        .setCheck(null)
        .appendField("do");
        this.appendStatementInput("falseBranch")
        .setCheck(null)
        .appendField("else");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['pin'] = {
      init: function() {
        this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown([["D0","D0"], ["D1","D1"], ["D2","D2"], ["D3","D3"], ["D4","D4"], ["D5","D5"], ["D6","D6"], ["D7","D7"], ["D8","D8"], ["D9","D9"], ["D10","D10"], ["D11","D11"], ["D12","D12"], ["D13","D13"], ["A0","A0"], ["A1","A1"], ["A2","A2"], ["A3","A3"], ["A4","A4"], ["A5","A5"]]), "pinNumber");
        this.setOutput(true, "Number");
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['get_sonar_distance_cm'] = {
      init: function() {
        this.appendDummyInput()
        .appendField("get distance from")
        .appendField(new Blockly.FieldDropdown([["sonar","sonar"]]), "sonarName")
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
      let block_id = block[0];
      let block_msg = block[1];
      Blockly.Blocks[block_id] = {
        init: function() {
          this.appendDummyInput()
          .appendField(i18n.translate(block_msg))
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
        let block_msg = i18n.translate("move ,, at speed");
        let fields = [
          [new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName"],
          [new Blockly.FieldDropdown([[i18n.translate("forward"),"fwd"],
          [i18n.translate("backward"),"bwd"]]), "direction"],
        ];
        let input = this.appendValueInput("speed").setCheck("Number");
        let msg_parts = block_msg.split(",");
        for (let i = 0; i < msg_parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(msg_parts[i]);
        }
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
        .appendField(i18n.translate("stop"))
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
        let block_msg = i18n.translate("set , speed to");
        let fields = [
          [new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName"],
        ];
        let input = this.appendValueInput("speed").setCheck("Number");
        let msg_parts = block_msg.split(",");
        for (let i = 0; i < msg_parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(msg_parts[i]);
        }
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
        let block_msg = i18n.translate("read distance from , in ,");
        let fields = [
          [new Blockly.FieldDropdown(currentSonarsForDropdown), "sonarName"],
          [new Blockly.FieldDropdown([["mm","mm"], ["cm","cm"], ["m","m"]]), "unit"],
        ];
        let input = this.appendDummyInput();
        let msg_parts = block_msg.split(",");
        for (let i = 0; i < msg_parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(msg_parts[i]);
        }
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };
  }

  function resizeBlockly () {
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

  function on (evt, callback) {
    observers[evt].push(callback);
  }

  function trigger(evt) {
    observers[evt].forEach(function (fn) {
      try {
        fn();
      } catch (err) {
        console.log(err);
      }
    });
  }

  function getGeneratedCode(){
    var xml = Blockly.Xml.workspaceToDom(workspace);
    return BlocksToAST.generate(xml, motors, sonars);
  }

  function refreshWorkspace() {
    fromXML(toXML());
  }

  function refreshToolbox() {
    workspace.toolbox_.refreshSelection();
  }

  function toXML() {
    return Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(workspace));
  }

  function fromXML(xml) {
    workspace.clear();
    Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(xml), workspace);
  }

  return {
    init: init,
    getMotors: function () { return motors; },
    setMotors: function (m) { motors = m; },
    getSonars: function () { return sonars; },
    setSonars: function (s) { sonars = s; },
    getWorkspace: function () { return workspace; },
    on: on,
    refreshToolbox: refreshToolbox,
    resizeWorkspace: resizeBlockly,
    toXML: toXML,
    fromXML: fromXML,
    getGeneratedCode: getGeneratedCode,
  }
})();
