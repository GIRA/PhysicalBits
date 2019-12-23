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

  // HACK(Richo): flat polyfill
  if (!Array.prototype.flat) {
    Object.defineProperty(Array.prototype, 'flat', {
      value: function(depth = 1) {
        return this.reduce(function (flat, toFlatten) {
          return flat.concat((Array.isArray(toFlatten) && (depth>1)) ? toFlatten.flat(depth-1) : toFlatten);
        }, []);
      }
    });
  }

  let version = 1;
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

        handleTaskBlocks(evt);
        handleProcedureBlocks(evt);
        handleFunctionBlocks(evt);
        handleVariableDeclarationBlocks(evt);
        trigger("change");
      });

      workspace.registerToolboxCategoryCallback("TASKS", function () {
        let node = XML.getChildNode(toolbox, "Tasks", "originalName");

        // Handle task declaring blocks. Make sure a new name is set by default to avoid collisions
        {
          let interestingBlocks = ["task", "timer"];
          let blocks = Array.from(node.getElementsByTagName("block"))
            .filter(block => interestingBlocks.includes(block.getAttribute("type")));

          let fields = blocks.map(function (block) {
            return Array.from(block.getElementsByTagName("field"))
              .filter(field => field.getAttribute("name") == "taskName");
          }).flat();

          let tasks = getCurrentScriptNames();
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

          let tasks = getCurrentTaskNames();
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


      workspace.registerToolboxCategoryCallback("PROCEDURES", function () {
        let node = XML.getChildNode(toolbox, "Procedures", "originalName");
        let nodes = Array.from(node.children);

        // Handle proc declaring blocks. Make sure a new name is set by default to avoid collisions
        {
          let interestingBlocks = ["proc_definition_0args", "proc_definition_1args",
                                   "proc_definition_2args", "proc_definition_3args"];
          let blocks = Array.from(node.getElementsByTagName("block"))
            .filter(block => interestingBlocks.includes(block.getAttribute("type")));

          let fields = blocks.map(function (block) {
            return Array.from(block.getElementsByTagName("field"))
              .filter(field => field.getAttribute("name") == "procName");
          }).flat();

          let defaultName = "default";
          let i = 1;
          let procs = getCurrentScriptNames();
          while (procs.includes(defaultName)) {
            defaultName = "default" + i;
            i++;
          }

          fields.forEach(field => field.innerText = defaultName);
        }

        // Handle procedure call blocks. Make sure they refer to the last existing proc by default.
        {
          let interestingBlocks = ["proc_call_0args", "proc_call_1args", "proc_call_2args", "proc_call_3args"];
          interestingBlocks.forEach(function (type, nargs) {
            let procs = getCurrentProcedureNames(nargs);
            if (procs.length == 0) {
              let index = nodes.findIndex(n => n.getAttribute("type") == type);
              if (index > -1) { nodes.splice(index, 1); }
            } else {
              let defaultName = procs.length > 0 ? procs[procs.length-1] : "default";
              Array.from(node.getElementsByTagName("block"))
                .filter(block => block.getAttribute("type") == type)
                .map(block => Array.from(block.getElementsByTagName("field"))
                    .filter(field => field.getAttribute("name") == "procName"))
                .flat()
                .forEach(field => field.innerText = defaultName);
              }
          });
        }

        return nodes;
      });


      workspace.registerToolboxCategoryCallback("FUNCTIONS", function () {
        let node = XML.getChildNode(toolbox, "Functions", "originalName");
        let nodes = Array.from(node.children);

        // Handle func declaring blocks. Make sure a new name is set by default to avoid collisions
        {
          let interestingBlocks = ["func_definition_0args", "func_definition_1args",
                                   "func_definition_2args", "func_definition_3args"];
          let blocks = Array.from(node.getElementsByTagName("block"))
            .filter(block => interestingBlocks.includes(block.getAttribute("type")));

          let fields = blocks.map(function (block) {
            return Array.from(block.getElementsByTagName("field"))
              .filter(field => field.getAttribute("name") == "funcName");
          }).flat();

          let defaultName = "default";
          let i = 1;
          let funcs = getCurrentScriptNames();
          while (funcs.includes(defaultName)) {
            defaultName = "default" + i;
            i++;
          }

          fields.forEach(field => field.innerText = defaultName);
        }

        // Handle function call blocks. Make sure they refer to the last existing func by default.
        {
          let interestingBlocks = ["func_call_0args", "func_call_1args", "func_call_2args", "func_call_3args"];
          interestingBlocks.forEach(function (type, nargs) {
            let funcs = getCurrentFunctionNames(nargs);
            if (funcs.length == 0) {
              let index = nodes.findIndex(n => n.getAttribute("type") == type);
              if (index > -1) { nodes.splice(index, 1); }
            } else {
              let defaultName = funcs.length > 0 ? funcs[funcs.length-1] : "default";
              Array.from(node.getElementsByTagName("block"))
                .filter(block => block.getAttribute("type") == type)
                .map(block => Array.from(block.getElementsByTagName("field"))
                    .filter(field => field.getAttribute("name") == "funcName"))
                .flat()
                .forEach(field => field.innerText = defaultName);
              }
          });
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
	let msg = i18n.translate("toggle pin %1");
	let inputFields = [
          () => this.appendValueInput("pinNumber")
                    .setCheck("Number")
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['wait'] = {
      init: function() {
        let msg = i18n.translate("wait %1 %2");
        let inputFields = [
          (input) => input.appendField(
            new Blockly.FieldDropdown([[i18n.translate("while"),"false"],
                                       [i18n.translate("until"),"true"]]), "negate"),
          () => this.appendValueInput("condition")
                    .setCheck("Boolean")
        ];

        initBlock(this, msg, inputFields);

        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['turn_pin_variable'] = {
      init: function() {
        let msg = i18n.translate("%1 pin %2");
        let inputFields = [
          input => input.appendField(new Blockly.FieldDropdown([[i18n.translate("turn on"), "on"],
                                                                [i18n.translate("turn off"), "off"]]),
                                                               "pinState"),
          () => this.appendValueInput("pinNumber").setCheck("Number")
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['is_pin_variable'] = {
      init: function() {
        let msg = i18n.translate("is %1 pin %2");
        let inputFields = [
          input => input.appendField(
            new Blockly.FieldDropdown([[i18n.translate("on"), "on"],
                                       [i18n.translate("off"), "off"]]),
            "pinState"),
          () => this.appendValueInput("pinNumber").setCheck("Number")
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setOutput(true, "Boolean");
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['read_pin_variable'] = {
      init: function() {
        let msg = i18n.translate("read pin %1");
        let inputFields = [
          () => this.appendValueInput("pinNumber").setCheck("Number")
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['write_pin_variable'] = {
      init: function() {
        let msg = i18n.translate("write pin %1 value %2");
        let inputFields = [
          () => this.appendValueInput("pinNumber")
                    .setCheck("Number")
                    .setAlign(Blockly.ALIGN_RIGHT),
          () => this.appendValueInput("pinValue")
                    .setCheck("Number")
                    .setAlign(Blockly.ALIGN_RIGHT)
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(0);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['forever'] = {
      init: function() {
        let msg = i18n.translate("repeat forever \n %1");
        let inputFields = [
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

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
        //this.setInputsInline(true);
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
        //this.setInputsInline(true);
        this.setOutput(true, null);
        this.setColour(20);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['elapsed_time'] = {
      init: function() {
        let msg = i18n.translate("elapsed %1");
        let inputFields = [
            () => {let field = this.appendDummyInput();
                   field.appendField(new Blockly.FieldDropdown(
                                     [[i18n.translate("milliseconds"),"ms"],
                                      [i18n.translate("seconds"),"s"],
                                      [i18n.translate("minutes"),"m"]]
                  ), "unit");
                  return field;
            }
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['degrees_servo_variable'] = {
      init: function() {
        let msg = i18n.translate("move servo on pin %1 degrees %2");
        let inputFields = [
          () => this.appendValueInput("pinNumber")
                    .setCheck("Number")
                    .setAlign(Blockly.ALIGN_RIGHT),
          () => this.appendValueInput("servoValue")
                    .setCheck("Number")
                    .setAlign(Blockly.ALIGN_RIGHT)
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
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
        let msg = i18n.translate("repeat %1 times \n %2");
        let inputFields = [
          () => this.appendValueInput("times")
                    .setCheck(null),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['for'] = {
      init: function() {
        let msg = i18n.translate("count with %1 from %2 to %3 by %4");
          let inputFields = [
              () => this.appendDummyInput()
                        .appendField(new Blockly.FieldDropdown(currentVariablesForDropdown),
                                     "variableName"),
              () => this.appendValueInput("start")
                        .setCheck("Number"),
              () => this.appendValueInput("stop")
                        .setCheck("Number"),
              () => this.appendValueInput("step")
                        .setCheck("Number")
          ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.appendStatementInput("statements").setCheck(null);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['timer'] = {
      init: function() {
        let msg = i18n.translate("timer named %1 running %2 times per %3 initial state %4 %5");
        let inputFields = [
          () => this.appendDummyInput()
            .appendField(new Blockly.FieldTextInput("default"), "taskName"),
          () => this.appendDummyInput()
            .appendField(new Blockly.FieldNumber(1000, 0, 999999), "runningTimes"),
          () => this.appendDummyInput()
            .appendField(new Blockly.FieldDropdown([[i18n.translate("second"),"s"],
                                                    [i18n.translate("minute"),"m"],
                                                    [i18n.translate("hour"),"h"]]), "tickingScale"),
          () => this.appendDummyInput()
            .appendField(new Blockly.FieldDropdown([[i18n.translate("started"),"started"],
                                                    [i18n.translate("stopped"),"stopped"]]), "initialState"),
          () => this.appendStatementInput("statements")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['task'] = {
      init: function() {
        let msg = i18n.translate("task named %1 %2");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "taskName"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
                    .setAlign(Blockly.ALIGN_RIGHT)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(175);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['conditional_simple'] = {
      init: function() {
        let msg = i18n.translate("if %1 then %2");
        let inputFields = [
          () => this.appendValueInput("condition")
                    .setCheck("Boolean")
                    .setAlign(Blockly.ALIGN_RIGHT),
          () => this.appendStatementInput("trueBranch")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(210);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['conditional_full'] = {
      init: function() {
        let msg = i18n.translate("if %1 then %2 else %3");
        let inputFields = [
          () => this.appendValueInput("condition")
                    .setCheck("Boolean")
                    .setAlign(Blockly.ALIGN_RIGHT),
          () => this.appendStatementInput("trueBranch")
                    .setCheck(null),
          () => this.appendStatementInput("falseBranch")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

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

    Blockly.Blocks['logical_compare'] = {
      init: function() {
        this.appendValueInput("left")
            .setCheck("Number");
        this.appendDummyInput()
            .appendField(new Blockly.FieldDropdown([[i18n.translate("="), "=="],
                                                    [i18n.translate("≠"), "!="],
                                                    [i18n.translate("<"), "<"],
                                                    [i18n.translate("≤"), "<="],
                                                    [i18n.translate(">"), ">"],
                                                    [i18n.translate("≥"), ">="]]), "operator");
        this.appendValueInput("right")
            .setCheck("Number");
        //this.setInputsInline(true);
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
        //this.setInputsInline(true);
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
        //this.setInputsInline(true);
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
        let msg = i18n.translate("remainder of %1 ÷ %2 \n");
        let inputFields = [
          () => this.appendValueInput("dividend")
                    .setCheck("Number")
                    .setAlign(Blockly.ALIGN_RIGHT),
          () => this.appendValueInput("divisor")
                    .setCheck("Number")
                    .setAlign(Blockly.ALIGN_RIGHT)
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['number_constrain'] = {
      init: function() {
        let msg = i18n.translate("constrain %1 low %2 high %3");
        let inputFields = [
            () => this.appendValueInput("value")
                      .setCheck("Number")
                      .setAlign(Blockly.ALIGN_RIGHT),
            () => this.appendValueInput("low")
                      .setCheck("Number")
                      .setAlign(Blockly.ALIGN_RIGHT),
            () => this.appendValueInput("high")
                      .setCheck("Number")
                      .setAlign(Blockly.ALIGN_RIGHT)
        ];

        initBlock(this, msg, inputFields);

        this.setInputsInline(true);
        this.setOutput(true, "Number");
        this.setColour(230);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };


    Blockly.Blocks['number_random_int'] = {
      init: function() {
        let msg = i18n.translate("random integer from %1 to %2");
        let inputFields = [
            () => this.appendValueInput("from")
                      .setCheck("Number")
                      .setAlign(Blockly.ALIGN_RIGHT),
            () => this.appendValueInput("to")
                      .setCheck("Number")
                      .setAlign(Blockly.ALIGN_RIGHT)
        ];

        initBlock(this, msg, inputFields);

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
        //this.setInputsInline(true);
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
    initProcedureBlocks();
    initFunctionBlocks();
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
        let msg = i18n.translate("move %1 %2 at speed %3");
        let inputFields = [
          input => input.appendField(
            new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName"),
          input => input.appendField(
            new Blockly.FieldDropdown([[i18n.translate("forward"),"fwd"],
                                       [i18n.translate("backward"),"bwd"]]), "direction"),
          () => this.appendValueInput("speed").setCheck("Number")
        ];

        initBlock(this, msg, inputFields);

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
        let msg = i18n.translate("set %1 speed to %2");
        let inputFields = [
          input => input.appendField(
            new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName"),
          () => this.appendValueInput("speed").setCheck("Number")
        ];

        initBlock(this, msg, inputFields);

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
        let msg = i18n.translate("read distance from %1 in %2");
        let inputFields = [
          input => input.appendField(
            new Blockly.FieldDropdown(currentSonarsForDropdown), "sonarName"),
          input => input.appendField(new Blockly.FieldDropdown([[i18n.translate("mm"),"mm"],
                                                                [i18n.translate("cm"),"cm"],
                                                                [i18n.translate("m"),"m"]]), "unit")
        ];

        initBlock(this, msg, inputFields);

        //this.setInputsInline(true);
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
        let msg = i18n.translate("set %1 to %2");
        let inputFields = [
          input => input.appendField(
            new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName"),
          () => this.appendValueInput("value").setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(330);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['increment_variable'] = {
      init: function() {
        let msg = i18n.translate("increment %1 by %2");
        let inputFields = [
          input => input.appendField(
            new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName"),
          () => this.appendValueInput("value").setCheck(null)
        ];

        initBlock(this, msg, inputFields);

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
        let msg = i18n.translate("declare local variable %1 with value %2");
        let fields = [
          input => input.appendField(new Blockly.FieldTextInput("temp"), "variableName"),
          () => this.appendValueInput("value").setCheck(null)
        ];
        initBlock(this, msg, fields);
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(330);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };
  }

  function initProcedureBlocks() {

    Blockly.Blocks['proc_definition_0args'] = {
      init: function() {
        let msg = i18n.translate("procedure named %1 %2");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "procName"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['proc_definition_1args'] = {
      init: function() {
        let msg = i18n.translate("procedure named %1 with argument %2 %3");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "procName"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg0"), "arg0"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['proc_definition_2args'] = {
      init: function() {
        let msg = i18n.translate("procedure named %1 with arguments %2 %3 %4");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "procName"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg0"), "arg0"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg1"), "arg1"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['proc_definition_3args'] = {
      init: function() {
        let msg = i18n.translate("procedure named %1 with arguments %2 %3 %4 %5");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "procName"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg0"), "arg0"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg1"), "arg1"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg2"), "arg2"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['return'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("exit"));
        this.setPreviousStatement(true, null);
        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['proc_call_0args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("execute"))
            .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(0)), "procName");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['proc_call_1args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("execute"))
            .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(1)), "procName");
        this.appendValueInput("arg0")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg0");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['proc_call_2args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("execute"))
            .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(2)), "procName");
        this.appendValueInput("arg0")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg0");
        this.appendValueInput("arg1")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg1");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(285);
     this.setTooltip("");
     this.setHelpUrl("");
      }
    };

    Blockly.Blocks['proc_call_3args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("execute"))
            .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(3)), "procName");
        this.appendValueInput("arg0")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg0");
        this.appendValueInput("arg1")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg1");
        this.appendValueInput("arg2")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg2");
        this.setPreviousStatement(true, null);
        this.setNextStatement(true, null);
        this.setColour(285);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

  }

  function initFunctionBlocks() {

    Blockly.Blocks['func_definition_0args'] = {
      init: function() {
        let msg = i18n.translate("function named %1 %2");
        let inputFields = [
          () => this.appendDummyInput()
            .appendField(new Blockly.FieldTextInput("default"), "funcName"),
          () => this.appendStatementInput("statements")
            .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['func_definition_1args'] = {
      init: function() {
        let msg = i18n.translate("function named %1 with argument %2 %3");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "funcName"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg0"), "arg0"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['func_definition_2args'] = {
      init: function() {
        let msg = i18n.translate("function named %1 with arguments %2 %3 %4");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "funcName"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg0"), "arg0"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg1"), "arg1"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['func_definition_3args'] = {
      init: function() {
        let msg = i18n.translate("function named %1 with arguments %2 %3 %4 %5");
        let inputFields = [
          () => this.appendDummyInput()
                    .appendField(new Blockly.FieldTextInput("default"), "funcName"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg0"), "arg0"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg1"), "arg1"),
          () => this.appendDummyInput()
                    .setAlign(Blockly.ALIGN_RIGHT)
                    .appendField(new Blockly.FieldTextInput("arg2"), "arg2"),
          () => this.appendStatementInput("statements")
                    .setCheck(null)
        ];

        initBlock(this, msg, inputFields);

        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['return_value'] = {
      init: function() {
        this.appendValueInput("value")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField(i18n.translate("return"));
        this.setPreviousStatement(true, null);
        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['func_call_0args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("evaluate"))
            .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(0)), "funcName");
        this.setOutput(true, null);
        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['func_call_1args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("evaluate"))
            .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(1)), "funcName");
        this.appendValueInput("arg0")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg0");
        this.setOutput(true, null);
        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };

    Blockly.Blocks['func_call_2args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("evaluate"))
            .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(2)), "funcName");
        this.appendValueInput("arg0")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg0");
        this.appendValueInput("arg1")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg1");
        this.setOutput(true, null);
        this.setColour(265);
     this.setTooltip("");
     this.setHelpUrl("");
      }
    };

    Blockly.Blocks['func_call_3args'] = {
      init: function() {
        this.appendDummyInput()
            .appendField(i18n.translate("evaluate"))
            .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(3)), "funcName");
        this.appendValueInput("arg0")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg0");
        this.appendValueInput("arg1")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg1");
        this.appendValueInput("arg2")
            .setCheck(null)
            .setAlign(Blockly.ALIGN_RIGHT)
            .appendField("arg2");
        this.setOutput(true, null);
        this.setColour(265);
        this.setTooltip("");
        this.setHelpUrl("");
      }
    };
  }

  function initBlock (block, msg, inputFields) {
    // if the translation msg contains line breaks, then
    // each part is created on separate rows
    let lineSeparator = "\n";
    if (msg.indexOf(lineSeparator) != -1) {
      msgRows = msg.split(lineSeparator);
      for (let i = 0; i < msgRows.length; i++) {
	let msgRow = msgRows[i];
	initBlock(block, msgRow, inputFields);
      }
      return;
    }
    // the translation msg or its separate rows are split into parts
    // for each input field reference and their corresponding
    // Blockly input fields are created together text labels
    let inputFieldRefPattern = /%\d+/g;
    let fieldRefMatch;
    let fieldRefNum;
    let msgUntilFieldRef;
    let previousRefMatchIndex = 0;
    let placeholders = new Set();
    while((fieldRefMatch = inputFieldRefPattern.exec(msg)) != null) {
        fieldRefNum = parseInt(fieldRefMatch[0].substring(1), 10) -1;
        msgUntilFieldRef = trim(msg.substring(previousRefMatchIndex, fieldRefMatch.index));
        previousRefMatchIndex = inputFieldRefPattern.lastIndex;

        let tempInputName = "___" + fieldRefNum + "___";
        let tempInput = block.appendDummyInput(tempInputName);
        let input = inputFields[fieldRefNum](tempInput);
        if (tempInput == input) {
          placeholders.add(input);
        } else {
          block.removeInput(tempInputName);
        }
        if (msgUntilFieldRef.length > 0) {
          input.insertFieldAt(0, msgUntilFieldRef);
        }
    }
    // append loose text if there exists any after the last input
    // field reference
    if (msg.length > previousRefMatchIndex) {
        let msgAfterLastFieldRef = trim(msg.substring(previousRefMatchIndex));
        let input = block.appendDummyInput();
        input.appendField(msgAfterLastFieldRef);
    }

    // merge placeholders into actual inputs
    let current = null;
    let inputs = block.inputList.slice();
    for (let i = inputs.length - 1; i >= 0; i--) {
      let input = inputs[i];
      if (placeholders.has(input)) {
        if (current != null) {
          let fields = input.fieldRow.slice();
          for (let j = fields.length - 1; j >= 0; j--) {
            let field = fields[j];
            if (field.prefixField) { field.prefixField ="";}
            if (field.suffixField) { field.suffixField ="";}
            input.removeField(field.name);
            current.insertFieldAt(0, field, field.name);
          }
          block.removeInput(input.name);
        }
      } else {
        current = input;
      }
    }
  }

  function getCurrentScriptNames() {
    // NOTE(Richo): This function returns all the scripts (task, proc, and func)
    return getCurrentTaskNames()
      .concat(getCurrentProcedureNames())
      .concat(getCurrentFunctionNames());
  }

  function getCurrentTaskNames() {
    let interestingBlocks = ["task", "timer"];
    return workspace.getAllBlocks()
      .filter(b => interestingBlocks.includes(b.type))
      .map(b => b.getFieldValue("taskName"));
  }

  function getCurrentProcedureNames(nargs) {
    let interestingBlocks = ["proc_definition_0args", "proc_definition_1args",
                             "proc_definition_2args", "proc_definition_3args"];
    if (nargs != undefined) { interestingBlocks = [interestingBlocks[nargs]]; }
    return workspace.getAllBlocks()
      .filter(b => interestingBlocks.includes(b.type))
      .map(b => b.getFieldValue("procName"));
  }

  function getCurrentFunctionNames(nargs) {
    let interestingBlocks = ["func_definition_0args", "func_definition_1args",
                             "func_definition_2args", "func_definition_3args"];
    if (nargs != undefined) { interestingBlocks = [interestingBlocks[nargs]]; }
    return workspace.getAllBlocks()
      .filter(b => interestingBlocks.includes(b.type))
      .map(b => b.getFieldValue("funcName"));
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

  function currentProceduresForDropdown(nargs) {
    let procs = getCurrentProcedureNames(nargs);
    if (procs.length == 0) return [["", ""]];
    return procs.map(function (name) { return [ name, name ]; });
  }

  function currentFunctionsForDropdown(nargs) {
    let funcs = getCurrentFunctionNames(nargs);
    if (funcs.length == 0) return [["", ""]];
    return funcs.map(function (name) { return [ name, name ]; });
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

  function handleProcedureBlocks(evt) {
    // NOTE(Richo): If a procedure is renamed we want to update all referencing blocks.
    let definitionBlocks = ["proc_definition_0args", "proc_definition_1args",
                            "proc_definition_2args", "proc_definition_3args"];
    let callBlocks = ["proc_call_0args", "proc_call_1args",
                      "proc_call_2args", "proc_call_3args"];
    if (evt.type == Blockly.Events.CHANGE
       && evt.element == "field"
       && evt.name == "procName") {
      let block = workspace.getBlockById(evt.blockId);
      if (block != undefined && definitionBlocks.includes(block.type)) {
        let callBlock = callBlocks[definitionBlocks.indexOf(block.type)];
        workspace.getAllBlocks()
          .filter(b => callBlock == b.type)
          .map(b => b.getField("procName"))
          .filter(f => f != undefined && f.getValue() == evt.oldValue)
          .forEach(f => f.setValue(evt.newValue));
      }
    }
  }

  function handleFunctionBlocks(evt) {
    // NOTE(Richo): If a function is renamed we want to update all referencing blocks.
    let definitionBlocks = ["func_definition_0args", "func_definition_1args",
                            "func_definition_2args", "func_definition_3args"];
    let callBlocks = ["func_call_0args", "func_call_1args",
                      "func_call_2args", "func_call_3args"];
    if (evt.type == Blockly.Events.CHANGE
       && evt.element == "field"
       && evt.name == "funcName") {
      let block = workspace.getBlockById(evt.blockId);
      if (block != undefined && definitionBlocks.includes(block.type)) {
        let callBlock = callBlocks[definitionBlocks.indexOf(block.type)];
        workspace.getAllBlocks()
          .filter(b => callBlock == b.type)
          .map(b => b.getField("funcName"))
          .filter(f => f != undefined && f.getValue() == evt.oldValue)
          .forEach(f => f.setValue(evt.newValue));
      }
    }
  }

  function handleTaskBlocks(evt) {
    // NOTE(Richo): If a task is renamed we want to update all referencing blocks.
    let interestingBlocks = ["task", "timer"];
    if (evt.type == Blockly.Events.CHANGE
       && evt.element == "field"
       && evt.name == "taskName") {
      let block = workspace.getBlockById(evt.blockId);
      if (block != undefined && interestingBlocks.includes(block.type)) {
        workspace.getAllBlocks()
          .filter(b => !interestingBlocks.includes(b.type))
          .map(b => b.getField("taskName"))
          .filter(f => f != undefined && f.getValue() == evt.oldValue)
          .forEach(f => f.setValue(evt.newValue));
      }
    }
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
     * NOTE(Richo): Procedure and Function definitions also create variables for their arguments
     */
    {
      let blocks = [
        {types: ["proc_definition_1args", "func_definition_1args"], fields: ["arg0"]},
        {types: ["proc_definition_2args", "func_definition_2args"], fields: ["arg0", "arg1"]},
        {types: ["proc_definition_3args", "func_definition_3args"], fields: ["arg0", "arg1", "arg2"]}
      ];
      blocks.forEach(function (block) {
        if (evt.type == Blockly.Events.CREATE && block.types.includes(evt.xml.getAttribute("type"))) {
          block.fields.forEach(function (fieldName) {
            let field = XML.getChildNode(evt.xml, fieldName);
            if (field != undefined) {
              let variableName = field.innerText;
              if (!variables.some(function (g) { return g.name == variableName})) {
                variables.push({ name: variableName });
              }
            }
          });
        }
      });
    }

    /*
     * NOTE(Richo): Renaming a procedure/function argument should update the variables.
     */
    {
      let interestingBlocks = [
        {types: ["proc_definition_1args", "func_definition_1args"], fields: ["arg0"]},
        {types: ["proc_definition_2args", "func_definition_2args"], fields: ["arg0", "arg1"]},
        {types: ["proc_definition_3args", "func_definition_3args"], fields: ["arg0", "arg1", "arg2"]},
      ];
      interestingBlocks.forEach(function (each) {
        if (evt.type == Blockly.Events.CHANGE
            && evt.element == "field"
            && each.fields.includes(evt.name)) {
          let block = workspace.getBlockById(evt.blockId);
          if (block != undefined && each.types.includes(block.type)) {
            let newName = evt.newValue;
            let oldName = evt.oldValue;
            renameVariable(oldName, newName, block);
          }
        }
      });
    }

    /*
     * NOTE(Richo): Renaming a local declaration should also update the variables.
     */
    if (evt.type == Blockly.Events.CHANGE
        && evt.element == "field"
        && evt.name == "variableName") {
      let block = workspace.getBlockById(evt.blockId);
      if (block != undefined && block.type == "declare_local_variable") {
        let newName = evt.newValue;
        let oldName = evt.oldValue;
        renameVariable(oldName, newName, block);
      }
    }
  }

  function renameVariable(oldName, newName, parentBlock) {

    // Create new variable, if it doesn't exist yet
    if (!variables.some(v => v.name == newName)) {
      let nextIndex = variables.length == 0 ? 0 : Math.max.apply(null, variables.map(function (v) { return v.index; })) + 1;
      let newVar = {index: nextIndex, name: newName};
      variables.push(newVar);
    }

    // Rename existing references to old variable (inside scope)
    workspace.getAllBlocks()
      .map(function (b) { return { block: b, field: b.getField("variableName") }; })
      .filter(function (o) {
        return o.field != undefined && o.field.getValue() == oldName;
      })
      .filter(function (o) {
        let current = o.block;
        do {
          if (current == parentBlock) return true;
          current = current.getParent();
        } while (current != undefined);
        return false;
      })
      .forEach(function (o) { o.field.setValue(newName); });

    // Remove old variable if not used
    let old = variables.find(v => v.name == oldName);
    if (old != undefined) {
      if (!getUsedVariables().has(oldName)) {
        let index = variables.indexOf(old);
        if (index > -1) {
          variables.splice(index, 1);
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
        .map(getVariableFieldsForBlock).flat()
        .map(f => f.getValue()));
  }

  function getVariableFieldsForBlock(block) {
    let interestingBlocks = {
      for: ["variableName"],
      declare_local_variable: ["variableName"],
      variable: ["variableName"],
      increment_variable: ["variableName"],
      set_variable: ["variableName"],
      proc_definition_1args: ["arg0"],
      proc_definition_2args: ["arg0", "arg1"],
      proc_definition_3args: ["arg0", "arg1", "arg2"],
      func_definition_1args: ["arg0"],
      func_definition_2args: ["arg0", "arg1"],
      func_definition_3args: ["arg0", "arg1", "arg2"],
    };
    return (interestingBlocks[block.type] || []).map(f => block.getField(f));
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
        .map(getVariableFieldsForBlock).flat()
        .forEach(function (field) {
          let value = renames.get(field.getValue());
          if (value == undefined) {
            // TODO(Richo): What do we do? Nothing...
          } else {
            field.setValue(value);
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
    getUsedVariables: getUsedVariables,
  }
})();
