let UziBlock = (function () {

  // HACK(Richo): trim polyfill
  if (!String.prototype.trim) {
    (function() {
      // Make sure we trim BOM and NBSP
      var rtrim = /^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g;
      String.prototype.trim = function() {
        return this.replace(rtrim, '');
      };
    })();
  }

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

  function trim(str) { return str.trim(); }

  function initCommonBlocks() {

    Blockly.Blocks['toggle_variable'] = {
      init: function() {
        this.appendValueInput("pinNumber")
          .setCheck("Number")
          .appendField(i18n.translate("toggle pin"));
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
          .appendField(i18n.translate("wait"))
          .appendField(new Blockly.FieldDropdown([[i18n.translate("while"),"false"],
                                                  [i18n.translate("until"),"true"]]), "negate");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['turn_pin_variable'] = {
      init: function() {
        let msg = i18n.translate("% pin");
        let fields = [
          [new Blockly.FieldDropdown([[i18n.translate("turn on"),"on"],
                                      [i18n.translate("turn off"),"off"]]), "pinState"],
        ];
        let input = this.appendValueInput("pinNumber").setCheck("Number");
        let parts = msg.split("%").map(trim);
        for (let i = 0; i < parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(parts[i]);
        }

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
        let msg = i18n.translate("is % pin");
        let fields = [
          [new Blockly.FieldDropdown([[i18n.translate("on"),"on"],
                                      [i18n.translate("off"),"off"]]), "pinState"],
        ];
        let input = this.appendValueInput("pinNumber").setCheck("Number");
        let parts = msg.split("%").map(trim);
        for (let i = 0; i < parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(parts[i]);
        }

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
          .appendField(i18n.translate("read pin"));
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['write_pin_variable'] = {
      init: function() {
        let msg = i18n.translate("write pin % value %");
        let parts = msg.split("%").map(trim);
        let inputs = [
          this.appendValueInput("pinNumber")
            .setCheck("Number")
            .setAlign(Blockly.ALIGN_RIGHT),
          this.appendValueInput("pinValue")
             .setCheck("Number")
             .setAlign(Blockly.ALIGN_RIGHT)
        ];
        for (let i = 0; i < parts.length; i++) {
          let input = i < inputs.length ? inputs[i] : this.appendDummyInput();
          let part = parts[i];
          input.appendField(part);
        }
        this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['forever'] = {
      init: function() {
        this.appendDummyInput()
          .appendField(i18n.translate("repeat forever"));
        this.appendStatementInput("statements")
          .setCheck(null);
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
          .appendField(i18n.translate("wait"));
        this.appendDummyInput()
          .appendField(new Blockly.FieldDropdown([[i18n.translate("milliseconds"),"ms"],
                                                  [i18n.translate("seconds"),"s"],
                                                  [i18n.translate("minutes"),"m"]]), "unit");
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
        .appendField(new Blockly.FieldTextInput(i18n.translate("This is a comment")), "comment")
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
          .appendField(new Blockly.FieldTextInput(i18n.translate("This is a comment")), "comment")
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
        let msg = i18n.translate("elapsed %");
        let fields = [
          [new Blockly.FieldDropdown([[i18n.translate("milliseconds"),"ms"],
                                      [i18n.translate("seconds"),"s"],
                                      [i18n.translate("minutes"),"m"]]), "unit"]
        ];
        let input = this.appendDummyInput();
        let parts = msg.split("%").map(trim);
        for (let i = 0; i < parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(parts[i]);
        }
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['degrees_servo_variable'] = {
      init: function() {
        let msg = i18n.translate("move servo on pin % degrees %");
        let parts = msg.split("%").map(trim);
        let inputs = [
          this.appendValueInput("pinNumber")
            .setCheck("Number")
            .setAlign(Blockly.ALIGN_RIGHT),
          this.appendValueInput("servoValue")
             .setCheck("Number")
             .setAlign(Blockly.ALIGN_RIGHT)
        ];
        for (let i = 0; i < parts.length; i++) {
          let input = i < inputs.length ? inputs[i] : this.appendDummyInput();
          let part = parts[i];
          input.appendField(part);
        }
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
          .appendField(i18n.translate("repeat"))
          .appendField(new Blockly.FieldDropdown([[i18n.translate("while"),"false"],
                                                  [i18n.translate("until"),"true"]]), "negate");
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
        let msg = i18n.translate("repeat % times");
        let parts = msg.split("%").map(trim);
        let inputs = [
          this.appendValueInput("times").setCheck(null),
          this.appendDummyInput(),
          this.appendStatementInput("statements").setCheck(null)
        ];
        for (let i = 0; i < parts.length; i++) {
          let input = i < inputs.length ? inputs[i] : this.appendDummyInput();
          let part = parts[i];
          input.appendField(part);
        }

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
        let msg = i18n.translate("count with % from % to % by %");
        let parts = msg.split("%").map(trim);
        let i = 0;
        this.appendDummyInput()
          .appendField(parts[i++])
          .appendField(new Blockly.FieldVariable("i"), "variable");
        this.appendValueInput("start")
          .setCheck("Number")
          .appendField(parts[i++]);
        this.appendValueInput("stop")
          .setCheck("Number")
          .appendField(parts[i++]);
        this.appendValueInput("step")
          .setCheck("Number")
          .appendField(parts[i++]);
        this.appendStatementInput("statements")
          .setCheck(null)
          .appendField(parts[i++]);

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
        let msg = i18n.translate("timer named % running % times per % initial state %");
        let parts = msg.split("%").map(trim);
        let i = 0;
        this.appendDummyInput()
          .appendField(parts[i++])
          .appendField(new Blockly.FieldTextInput("default"), "taskName");
        this.appendDummyInput()
          .appendField(parts[i++])
          .appendField(new Blockly.FieldNumber(1000, 0, 999999), "runningTimes")
          .appendField(parts[i++])
          .appendField(new Blockly.FieldDropdown([[i18n.translate("second"),"s"],
                                                  [i18n.translate("minute"),"m"],
                                                  [i18n.translate("hour"),"h"]]), "tickingScale");
        this.appendDummyInput()
          .appendField(parts[i++])
          .appendField(new Blockly.FieldDropdown([[i18n.translate("started"),"started"],
                                                  [i18n.translate("stopped"),"stopped"]]), "initialState");
        this.appendStatementInput("statements")
          .setCheck(null)
          .setAlign(Blockly.ALIGN_RIGHT)
          .appendField(parts[i++]);
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['task'] = {
      init: function() {
        this.appendDummyInput()
          .appendField(i18n.translate("task named"))
          .appendField(new Blockly.FieldTextInput("default"), "taskName");
        this.appendStatementInput("statements")
          .setCheck(null)
          .setAlign(Blockly.ALIGN_RIGHT);
        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['conditional_simple'] = {
      init: function() {
        let msg = i18n.translate("if % then %");
        let parts = msg.split("%").map(trim);
        let i = 0;
        this.appendValueInput("condition")
          .setCheck("Boolean")
          .setAlign(Blockly.ALIGN_RIGHT)
          .appendField(parts[i++]);
        this.appendStatementInput("trueBranch")
          .setCheck(null)
          .appendField(parts[i++]);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['conditional_full'] = {
      init: function() {
        let msg = i18n.translate("if % then % else %");
        let parts = msg.split("%").map(trim);
        let i = 0;
        this.appendValueInput("condition")
          .setCheck("Boolean")
          .setAlign(Blockly.ALIGN_RIGHT)
          .appendField(parts[i++]);
        this.appendStatementInput("trueBranch")
          .setCheck(null)
          .appendField(parts[i++]);
        this.appendStatementInput("falseBranch")
          .setCheck(null)
          .appendField(parts[i++]);
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
        let msg = i18n.translate("move %% at speed");
        let fields = [
          [new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName"],
          [new Blockly.FieldDropdown([[i18n.translate("forward"),"fwd"],
                                      [i18n.translate("backward"),"bwd"]]), "direction"],
        ];
        let input = this.appendValueInput("speed").setCheck("Number");
        let parts = msg.split("%").map(trim);
        for (let i = 0; i < parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(parts[i]);
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
        let msg = i18n.translate("set % speed to");
        let fields = [
          [new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName"],
        ];
        let input = this.appendValueInput("speed").setCheck("Number");
        let parts = msg.split("%").map(trim);
        for (let i = 0; i < parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(parts[i]);
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
        let msg = i18n.translate("read distance from % in %");
        let fields = [
          [new Blockly.FieldDropdown(currentSonarsForDropdown), "sonarName"],
          [new Blockly.FieldDropdown([[i18n.translate("mm"),"mm"],
                                      [i18n.translate("cm"),"cm"],
                                      [i18n.translate("m"),"m"]]), "unit"],
        ];
        let input = this.appendDummyInput();
        let parts = msg.split("%").map(trim);
        for (let i = 0; i < parts.length; i++) {
          if (i > 0) {
            input.appendField(fields[i - 1][0], fields[i - 1][1]);
          }
          input.appendField(parts[i]);
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
