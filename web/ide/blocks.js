let UziBlock = (function () {

  // HACK(Richo): trim polyfill
  if (!String.prototype.trim) {
    (function() {
      // Make sure we trim BOM and NBSP
      let rtrim = /^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g;
      String.prototype.trim = function() {
        return this.replace(rtrim, '');
      };
    })();
  }

  let version = 0;
  let blocklyArea, blocklyDiv, workspace;
  let motors = [];
  let sonars = [];
  let variables = [];
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
      if (typeof(toolbox) == "string") {
        toolbox = Blockly.Xml.textToDom(toolbox);
      } else {
        toolbox = toolbox.documentElement;
      }
      let categories = toolbox.getElementsByTagName("category");
      for (let i = 0; i < categories.length; i++) {
        let category = categories[i];
        category.setAttribute("originalName", category.getAttribute("name"));
        category.setAttribute("name", i18n.translate(category.getAttribute("originalName")));
      }
      let buttons = toolbox.getElementsByTagName("button");
      for (let i = 0; i < buttons.length; i++) {
        let button = buttons[i];
        button.setAttribute("originalText", button.getAttribute("text"));
        button.setAttribute("text", i18n.translate(button.getAttribute("originalText")));
      }
      workspace = Blockly.inject(blocklyDiv, {
        toolbox: toolbox,
        zoom: {
          controls: true,
          wheel: true,
          startScale: 0.85,
          maxScale: 3,
          minScale: 0.3,
          scaleSpeed: 1.03
        },
        media: "libs/google-blockly/media/"
      });

      i18n.on("change", function () {
        for (let i = 0; i < categories.length; i++) {
          let category = categories[i];
          category.setAttribute("name", i18n.translate(category.getAttribute("originalName")));
        }
        for (let i = 0; i < buttons.length; i++) {
          let button = buttons[i];
          button.setAttribute("text", i18n.translate(button.getAttribute("originalText")));
        }
        workspace.updateToolbox(toolbox);
        refreshToolbox();
      });

      workspace.addChangeListener(function (evt) {
        if (evt.type == Blockly.Events.UI) return; // Ignore these events

        handleVariableDeclarationBlocks(evt);
        trigger("change");
      });

      workspace.registerToolboxCategoryCallback("TASKS", function () {
        let node = XML.getChildNode(toolbox, "Tasks", "originalName");
        let tasks = getCurrentTaskNames();

        // Handle task declaring blocks. Make sure a new name is set by default to avoid collisions
        {
          let interestingBlocks = ["task", "timer"];
          let blocks = Array.from(node.getElementsByTagName("block"))
            .filter(block => interestingBlocks.includes(block.getAttribute("type")));

          let fields = blocks.map(function (block) {
            return Array.from(block.getElementsByTagName("field"))
              .filter(field => field.getAttribute("name") == "taskName");
          }).flat();

          let defaultName = "default";
          let i = 1;
          while (tasks.includes(defaultName)) {
            defaultName = "default" + i;
            i++;
          }

          fields.forEach(field => field.innerText = defaultName);
        }

        // Handle task control blocks. Make sure they refer to the last existing task by default.
        {
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


          let defaultName = tasks.length > 0 ? tasks[tasks.length-1] : "default";
          fields.forEach(field => field.innerText = defaultName);
        }

        return Array.from(node.children);
      });

      workspace.registerToolboxCategoryCallback("DC_MOTORS", function () {
        let node = XML.getChildNode(XML.getChildNode(toolbox, "Motors", "originalName"), "DC", "originalName");
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
        let node = XML.getChildNode(XML.getChildNode(toolbox, "Sensors", "originalName"), "Sonar", "originalName");
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

      workspace.registerToolboxCategoryCallback("VARIABLES", function () {
        let node = XML.getChildNode(toolbox, "Variables", "originalName");
        let nodes = Array.from(node.children);
        if (variables.length == 0) {
          nodes.splice(2); // Leave the button and declare_local_variable
        } else {
          let fields = node.getElementsByTagName("field");
          for (let i = 1; i < fields.length; i++) {
            let field = fields[i];
            if (field.getAttribute("name") === "variableName") {
              field.innerText = variables[variables.length-1].name;
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
          .appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName");
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

    Blockly.Blocks['boolean'] = {
      init: function() {
        this.appendDummyInput()
          .appendField(new Blockly.FieldDropdown([[i18n.translate("true"), "true"],
                                                  [i18n.translate("false"), "false"]]), "value");
        this.setOutput(true, "Boolean");
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['logical_operation'] = {
      init: function() {
        this.appendValueInput("left")
            .setCheck("Boolean");
        this.appendDummyInput()
            .appendField(new Blockly.FieldDropdown([[i18n.translate("and"),"and"], [i18n.translate("or"),"or"]]), "operator");
        this.appendValueInput("right")
            .setCheck("Boolean");
        this.setInputsInline(true);
        this.setOutput(true, "Boolean");
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['logical_not'] = {
      init: function() {
        this.appendValueInput("value")
            .setCheck("Boolean")
            .appendField(i18n.translate("not"));
        this.setOutput(true, "Boolean");
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_property'] = {
      init: function() {
        this.appendValueInput("value")
            .setCheck("Number");
        this.appendDummyInput()
            .appendField(new Blockly.FieldDropdown([[i18n.translate("is even"),"even"],
                                                    [i18n.translate("is odd"),"odd"],
                                                    [i18n.translate("is prime"),"prime"],
                                                    [i18n.translate("is whole"),"whole"],
                                                    [i18n.translate("is positive"),"positive"],
                                                    [i18n.translate("is negative"),"negative"]]), "property");
        this.setOutput(true, "Boolean");
        this.setColour(225);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_divisibility'] = {
      init: function() {
        this.appendValueInput("left")
            .setCheck("Number");
        this.appendDummyInput()
            .appendField(i18n.translate("is divisible by"));
        this.appendValueInput("right")
            .setCheck("Number");
        this.setOutput(true, "Boolean");
        this.setColour(225);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(new Blockly.FieldNumber(0), "value");
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_operation'] = {
      init: function() {
        this.appendValueInput("number")
            .setCheck("Number")
            .appendField(new Blockly.FieldDropdown([[i18n.translate("square root"),"sqrt"],
                                                    [i18n.translate("absolute"),"abs"],
                                                    [i18n.translate("-"),"negate"],
                                                    [i18n.translate("ln"),"ln"],
                                                    [i18n.translate("log10"),"log10"],
                                                    [i18n.translate("e^"),"exp"],
                                                    [i18n.translate("10^"),"pow10"]]), "operator");
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_trig'] = {
      init: function() {
        this.appendValueInput("number")
            .setCheck("Number")
            .appendField(new Blockly.FieldDropdown([[i18n.translate("sin"),"sin"],
                                                    [i18n.translate("cos"),"cos"],
                                                    [i18n.translate("tan"),"tan"],
                                                    [i18n.translate("asin"),"asin"],
                                                    [i18n.translate("acos"),"acos"],
                                                    [i18n.translate("atan"),"atan"]]), "operator");
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_round'] = {
      init: function() {
        this.appendValueInput("number")
            .setCheck("Number")
            .appendField(new Blockly.FieldDropdown([[i18n.translate("round"),"round"],
                                                    [i18n.translate("round up"),"ceil"],
                                                    [i18n.translate("round down"),"floor"]]), "operator");
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_modulo'] = {
      init: function() {
        let msg = i18n.translate("remainder of % ÷ %");
        let parts = msg.split("%").map(trim);
        let inputs = [
          this.appendValueInput("dividend")
            .setCheck("Number")
            .setAlign(Blockly.ALIGN_RIGHT),
          this.appendValueInput("divisor")
             .setCheck("Number")
             .setAlign(Blockly.ALIGN_RIGHT)
        ];
        for (let i = 0; i < parts.length; i++) {
          let input = i < inputs.length ? inputs[i] : this.appendDummyInput();
          let part = parts[i];
          input.appendField(part);
        }
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_constrain'] = {
      init: function() {
        let msg = i18n.translate("constrain % low % high %");
        let parts = msg.split("%").map(trim);
        let inputs = [
          this.appendValueInput("value")
            .setCheck("Number")
            .setAlign(Blockly.ALIGN_RIGHT),
          this.appendValueInput("low")
             .setCheck("Number")
             .setAlign(Blockly.ALIGN_RIGHT),
           this.appendValueInput("high")
              .setCheck("Number")
              .setAlign(Blockly.ALIGN_RIGHT)
        ];
        for (let i = 0; i < parts.length; i++) {
          let input = i < inputs.length ? inputs[i] : this.appendDummyInput();
          let part = parts[i];
          input.appendField(part);
        }
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_random_int'] = {
      init: function() {
        let msg = i18n.translate("random integer from % to %");
        let parts = msg.split("%").map(trim);
        let inputs = [
          this.appendValueInput("from")
            .setCheck("Number")
            .setAlign(Blockly.ALIGN_RIGHT),
          this.appendValueInput("to")
             .setCheck("Number")
             .setAlign(Blockly.ALIGN_RIGHT)
        ];
        for (let i = 0; i < parts.length; i++) {
          let input = i < inputs.length ? inputs[i] : this.appendDummyInput();
          let part = parts[i];
          input.appendField(part);
        }
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_random_float'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("random fraction"));
        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };
  }

  function initSpecialBlocks() {
    initTaskBlocks();
    initDCMotorBlocks();
    initSonarBlocks();
    initVariableBlocks();
  }

  function initTaskBlocks() {

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

  function initVariableBlocks() {

    Blockly.Blocks['set_variable'] = {
      init: function() {
        this.appendValueInput("value")
            .setCheck(null)
            .appendField("set")
            .appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName")
            .appendField("to");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(330);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['increment_variable'] = {
      init: function() {
        this.appendValueInput("value")
            .setCheck(null)
            .appendField("increment")
            .appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName")
            .appendField("by");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(330);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };


    Blockly.Blocks['variable'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName");
        this.setOutput(true, null);
        this.setColour(330);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['declare_local_variable'] = {
      init: function() {
        this.appendValueInput("value")
            .setCheck(null)
            .appendField("declare local variable")
            .appendField(new Blockly.FieldTextInput("temp"), "variableName")
            .appendField("with value");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(330);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };
  }


  function getCurrentTaskNames() {
    let program = Uzi.state.program.current;
    if (program == null) return [];

    // HACK(Richo): Filtering by the class name...
    return program.ast.scripts
      .filter(function (s) { return s.__class__ == "UziTaskNode"; })
      .map(function (each) { return each.name; });
  }

  function getDefaultTaskName() {
    let names = getCurrentTaskNames();
    let def = "default";
    let i = 1;
    while (names.includes(def)) {
      def = "default" + i;
      i++;
    }
    return def;
  }

  function currentTasksForDropdown() {
    let tasks = getCurrentTaskNames();
    if (tasks.length == 0) return [["", ""]];
    return tasks.map(function (name) { return [ name, name ]; });
  }

  function currentMotorsForDropdown() {
    if (motors.length == 0) return [["", ""]];
    return motors.map(function(each) { return [ each.name, each.name ]; });
  }

  function currentSonarsForDropdown() {
    if (sonars.length == 0) return [["", ""]];
    return sonars.map(function(each) { return [ each.name, each.name ]; });
  }

  function currentVariablesForDropdown() {
    if (variables.length == 0) return [["", ""]];
    return variables.map(function(each) { return [ each.name, each.name ]; });
  }

  function handleVariableDeclarationBlocks(evt) {
    /*
     * NOTE(Richo): Some blocks automatically add variables when created. Here we
     * handle the creation event of such blocks.
     */
    {
      let blocks = ["for", "declare_local_variable"];
      if (evt.type == Blockly.Events.CREATE && blocks.includes(evt.xml.getAttribute("type"))) {
        let field = XML.getChildNode(evt.xml, "variableName");
        if (field != undefined) {
          let variableName = field.innerText;
          if (!variables.some(function (g) { return g.name == variableName})) {
            variables.push({ name: variableName });
          }
        }
      }
    }

    /*
     * NOTE(Richo): Renaming a local declaration should also update the variables.
     */
    if (evt.type == Blockly.Events.CHANGE
        && evt.element == "field"
        && evt.name == "variableName") {
      let block = workspace.getBlockById(evt.blockId);
      if (block != undefined && block.type == "declare_local_variable") {

        // Create new variable, if it doesn't exist yet
        if (!variables.some(v => v.name == evt.newValue)) {
          let nextIndex = variables.length == 0 ? 0 : Math.max.apply(null, variables.map(function (v) { return v.index; })) + 1;
          let newVar = {index: nextIndex, name: evt.newValue};
          variables.push(newVar);
        }

        // Rename existing references to old variable (inside scope)
        workspace.getAllBlocks()
          .map(function (b) { return { block: b, field: b.getField("variableName") }; })
          .filter(function (o) {
            return o.field != undefined && o.field.getValue() == evt.oldValue;
          })
          .filter(function (o) {
            let current = o.block;
            do {
              if (current == block) return true;
              current = current.getParent();
            } while (current != undefined);
            return false;
          })
          .forEach(function (o) { o.field.setValue(evt.newValue); });

        // Remove old variable if not used
        let old = variables.find(v => v.name == evt.oldValue);
        if (old != undefined) {
          if (!getUsedVariables().has(evt.oldValue)) {
            let index = variables.indexOf(old);
            if (index > -1) {
              variables.splice(index, 1);
            }
          }
        }
      }
    }
  }

  function resizeBlockly () {
    // Only if Blockly was initialized
    if (workspace == undefined) return;

    let x, y;
    x = y = 0;
    blocklyDiv.style.left = x + 'px';
    blocklyDiv.style.top = y + 'px';
    let scale = 1;
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
    let xml = Blockly.Xml.workspaceToDom(workspace);
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

  function getUsedVariables() {
    return new Set(workspace.getAllBlocks()
        .map(b => b.getField("variableName"))
        .filter(f => f != undefined)
        .map(f => f.getValue()));
  }

  return {
    init: init,
    on: on,
    refreshToolbox: refreshToolbox,
    resizeWorkspace: resizeBlockly,
    toXML: toXML,
    fromXML: fromXML,
    getGeneratedCode: getGeneratedCode,

    getWorkspace: function () { return workspace; },
    getMotors: function () { return motors; },
    setMotors: function (data) {
      let renames = new Map();
      data.forEach(function (m) {
        if (motors[m.index] == undefined) return;
        renames.set(motors[m.index].name, m.name);
      });

      workspace.getAllBlocks()
        .map(b => ({ block: b, field: b.getField("motorName") }))
        .filter(o => o.field != undefined)
        .forEach(function (o) {
          let value = renames.get(o.field.getValue());
          if (value == undefined) {
            o.block.dispose(true);
          } else {
            o.field.setValue(value);
          }
        });

      motors = data;
    },
    getSonars: function () { return sonars; },
    setSonars: function (data) {
      let renames = new Map();
      data.forEach(function (m) {
        if (sonars[m.index] == undefined) return;
        renames.set(sonars[m.index].name, m.name);
      });

      workspace.getAllBlocks()
        .map(b => ({ block: b, field: b.getField("sonarName") }))
        .filter(o => o.field != undefined)
        .forEach(function (o) {
          let value = renames.get(o.field.getValue());
          if (value == undefined) {
            o.block.dispose(true);
          } else {
            o.field.setValue(value);
          }
        });

      sonars = data;
    },
    getVariables: function () { return variables; },
    setVariables: function (data) {
      let renames = new Map();
      data.forEach(function (m) {
        if (variables[m.index] == undefined) return;
        renames.set(variables[m.index].name, m.name);
      });

      workspace.getAllBlocks()
        .map(b => ({ block: b, field: b.getField("variableName") }))
        .filter(o => o.field != undefined)
        .forEach(function (o) {
          let value = renames.get(o.field.getValue());
          if (value == undefined) {
            // TODO(Richo): What do we do? Nothing...
          } else {
            o.field.setValue(value);
          }
        });

      variables = data;
    },
    getDataForStorage: function () {
      return {
        version: version,
        blocks: toXML(),
        motors: motors,
        sonars: sonars,
        variables: variables,
      };
    },
    setDataFromStorage: function (d) {
      // Check compatibility
      if (d.version != version) { return; }

      fromXML(d.blocks);
      motors = d.motors || [];
      sonars = d.sonars || [];
      variables = d.variables || [];
    },
    getUsedVariables: getUsedVariables
  }
})();
