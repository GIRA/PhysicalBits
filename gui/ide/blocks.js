let UziBlock = (function () {

  let version = 4;
  let blocklyArea, blocklyDiv, workspace;
  let timestamps = new Map();
  let userInteraction = false; // Flat to indicte that a workspace evt comes from the user
  let ignoreChanges = false; // Flag to indicate that the next workspace change evt must be ignored
  let selectedBlock = null;
  let readOnly = false;  
  let readOnlyProgram = null;
  let motors = [];
  let lcds = [];
  let sonars = [];
  let joysticks = [];
  let variables = [];
  let lists = [];
  let observers = {
    "change" : [],
    "select": []
  };

  let uziSyntax = false;
  let toolbox = null;

  /**
  NOTE(Richo): This function should be used instead of i18n.translate in all block
  definitions. It will translate the text only if the uziSyntax flag is not set.
  **/
  function blocklyTranslate(text) {
    return uziSyntax ? text : i18n.translate(text);
  }

  const colors = {
    HIDDEN: "#9E8E7F",
    TASKS: 175,
    GPIO: 345,
    PINS: 345,
    MOTORS: 0,
    LCD: 0,
    SERVO: 0,
    "DC MOTOR": 0,
    SENSORS: 15,
    SONAR: 15,
    BUTTONS: 15,
    JOYSTICK: 15,
    SOUND: 30,
    CONTROL: 140,
    STRINGS: 235,
    MATH: 210,
    LOGIC: 210,
    VARIABLES: 305,
    LISTS: 305,
    PROCEDURES: 285,
    FUNCTIONS: 265,
  }

  const types = {
    PIN: "pin",
    NUMBER: "number",
    BOOLEAN: "boolean",
    STRING: "string"
  };

  function allTypes(preferredType) {
    let result = [];
    if (preferredType) {
      result.push(preferredType);
    }
    for (let key in types) {
      let type = types[key];
      if (type != preferredType) {
        result.push(type);
      }
    }
    return result;
  }

  const spec = {
    // TODO(Richo)
    here_be_dragons_stmt: {
      text: "%here_be_dragons",
      type: null,
      inputs: {
        "here_be_dragons": {
          name: "code",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput("HERE BE DRAGONS"), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.HIDDEN,
      supportsBreakpoints: false,
      postload: function (block) {
        block.setEditable(false);
      },
    },
    here_be_dragons_expr: {
      text: "%here_be_dragons",
      type: null,
      inputs: {
        "here_be_dragons": {
          name: "code",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput("HERE BE DRAGONS"), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.HIDDEN,
      supportsBreakpoints: false,
      postload: function (block) {
        block.setEditable(false);
      },
    },
    here_be_dragons_script: {
      text: "%here_be_dragons",
      type: null,
      inputs: {
        "here_be_dragons": {
          name: "code",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput("HERE BE DRAGONS"), name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.HIDDEN,
      isTopLevel: true,
      supportsBreakpoints: false,
      postload: function (block) {
        block.setEditable(false);
      },
    },

    // Secret
    yield: {
      text: "yield ;",
      type: null,
      inputs: {},
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    secret_return: { // NOTE(Richo): Special version of return that accepts bottom connections
      text: "exit ;",
      type: null,
      inputs: {},
      connections: { up: true, down: true, left: false },
      color: colors.PROCEDURES,
      supportsBreakpoints: true,
    },
    secret_return_value: { // NOTE(Richo): Special version of return_value that accepts bottom connections
      text: "return %value ;",
      type: null,
      inputs: {
        "value": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.FUNCTIONS,
      supportsBreakpoints: true,
    },

    // Imports
    import: {
      text: "import %1 from %2",
      type: null,
      inputs: {
        "1": {
          name: "alias",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput(""), name),
        },
        "2": {
          name: "path",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput(""), name),
        }
      },
      connections: { up: false, down: false, left: false},
      color: colors.TASKS,
      postload: function (block) {
        block.setEditable(false);
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },

    // Tasks
    task: {
      text: "task %1 () { \n %2 }",
      type: null,
      inputs: {
        "1": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput("default"), name),
        },
        "2": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.TASKS,
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    timer: {
      text: "task %1 () %4 %2 / %3 { \n %5 }",
      type: null,
      inputs: {
        "1": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput("default"), name),
        },
        "2": {
          name: "runningTimes",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldNumber(1000, 0, 999999), name),
        },
        "3": {
          name: "tickingScale",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("s"),"s"],
                                                                           [blocklyTranslate("m"),"m"],
                                                                           [blocklyTranslate("h"),"h"]]), name),
        },
        "4": {
          name: "initialState",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("running"),"started"],
                                                                           [blocklyTranslate("stopped"),"stopped"]]), name),
        },
        "5": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.TASKS,
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    start_task: {
      text: "start %name ;",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.TASKS,
      supportsBreakpoints: true,
    },
    pause_task: {
      text: "pause %name ;",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.TASKS,
      supportsBreakpoints: true,
    },
    stop_task: {
      text: "stop %name ;",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.TASKS,
      supportsBreakpoints: true,
    },
    run_task: {
      text: "%taskName () ;",
      type: null,
      inputs: {
        "taskName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.TASKS,
      supportsBreakpoints: true,
    },
    resume_task: {
      text: "resume %name ;",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.TASKS,
      supportsBreakpoints: true,
    },

    // GPIO
    toggle_pin: {
      text: "toggle( %1 ) ;",
      type: null,
      inputs: {
        "1": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.GPIO,
      supportsBreakpoints: true,
    },
    turn_onoff_pin: {
      text: "turn %1 ( %2 ) ;",
      type: null,
      inputs: {
        "1": {
          name: "pinState",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("On"), "on"],
                                                                           [blocklyTranslate("Off"), "off"]]),
                                                                           name),
        },
        "2": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.GPIO,
      supportsBreakpoints: true,
    },
    is_onoff_pin: {
      text: "%1 ( %2 )",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          name: "pinState",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("isOn"), "on"],
                                                                           [blocklyTranslate("isOff"), "off"]]),
                                                                          name),
        },
        "2": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.GPIO,
      supportsBreakpoints: true,
    },
    read_pin: {
      text: "read( %1 )",
      type: types.NUMBER,
      inputs: {
        "1": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.GPIO,
      supportsBreakpoints: true,
    },
    write_pin: {
      text: "write( %1 , %2 );",
      type: null,
      inputs: {
        "1": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "pinValue",
          types: [types.NUMBER, types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.GPIO,
      supportsBreakpoints: true,
    },
    set_pin_mode: {
      text: "setPinMode( %1 , %2 );",
      type: null,
      inputs: {
        "1": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "mode",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("INPUT"),"INPUT"],
                                                                                  [blocklyTranslate("OUTPUT"),"OUTPUT"],
                                                                                  [blocklyTranslate("INPUT PULLUP"),"INPUT_PULLUP"]]), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.GPIO,
      supportsBreakpoints: true,
    },
    pin: {
      text: "%pin",
      type: types.PIN,
      inputs: {
        "pin": {
          name: "pinNumber",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                         .appendField(new Blockly.FieldDropdown([["D0","D0"], ["D1","D1"], ["D2","D2"], ["D3","D3"],
                                                                                 ["D4","D4"], ["D5","D5"], ["D6","D6"], ["D7","D7"],
                                                                                 ["D8","D8"], ["D9","D9"], ["D10","D10"], ["D11","D11"],
                                                                                 ["D12","D12"], ["D13","D13"], ["A0","A0"], ["A1","A1"],
                                                                                 ["A2","A2"], ["A3","A3"], ["A4","A4"], ["A5","A5"]]),
                                                                                name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.GPIO,
      supportsBreakpoints: false,
    },
    pin_cast: {
      text: "pin ( %1 )",
      type: types.PIN,
      inputs: {
        "1": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.GPIO,
      supportsBreakpoints: false,
    },

    // Motors - Servo
    set_servo_degrees: {
      text: "setServoDegrees( %1 , %2 ) ;",
      type: null,
      inputs: {
        "1": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "servoValue",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.MOTORS,
      supportsBreakpoints: true,
    },
    get_servo_degrees: {
      text: "getServoDegrees( %1 ) ;",
      type: types.NUMBER,
      inputs: {
        "1": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MOTORS,
      supportsBreakpoints: true,
    },

    // Motors - DC
    move_dcmotor: {
      text: "%name . %direction (speed: %speed ) ;",
      type: null,
      inputs: {
        "name": {
          name: "motorName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), name),
        },
        "direction": {
          name: "direction",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("forward"),"fwd"],
                                                                           [blocklyTranslate("backward"),"bwd"]]), name),
        },
        "speed": {
          name: "speed",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.MOTORS,
      supportsBreakpoints: true,
    },
    stop_dcmotor: {
      text: "%name . brake() ;",
      type: null,
      inputs: {
        "name": {
          name: "motorName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.MOTORS,
      supportsBreakpoints: true,
    },
    change_speed_dcmotor: {
      text: "%name . setSpeed (speed: %speed ) ;",
      type: null,
      inputs: {
        "name": {
          name: "motorName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), name),
        },
        "speed": {
          name: "speed",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.MOTORS,
      supportsBreakpoints: true,
    },
    get_speed_dcmotor: {
      text: "%name .getSpeed( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "motorName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.MOTORS,
      supportsBreakpoints: true,
    },

    // LCDs
    print_number : {
      text: "%name . printNumber ( %number ) ;",
      type: null,
      inputs: {
        "name": {
          name: "lcdName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentLcdsForDropdown), name),
        },
        "number": {
          name: "number",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.LCD,
      supportsBreakpoints: true,
    },
    print_string : {
          text: "%name . printString ( %string ) ;",
          type: null,
          inputs: {
            "name": {
              name: "lcdName",
              types: null,
              builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentLcdsForDropdown), name),
            },
            "string": {
              name: "string",
              types: [types.STRING],
              builder: (block, input, name) => block.appendValueInput(name),
            }
          },
          connections: { up: true, down: true, left: false },
          color: colors.LCD,
          supportsBreakpoints: true,
    },
    lcd_set_cursor: {
      text: "%name . setCursor ( %column, %row ) ;",
      type: null,
      inputs: {
        "name": {
          name: "lcdName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentLcdsForDropdown), name),
        },
        "column": {
          name: "column",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name).setAlign(Blockly.ALIGN_RIGHT),
        },
        "row": {
          name: "row",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name).setAlign(Blockly.ALIGN_RIGHT),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.LCD,
      supportsBreakpoints: true,
    },
    lcd_clear: {
      text: "%name . clear () ;",
      type: null,
      inputs: {
        "name": {
          name: "lcdName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentLcdsForDropdown), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.LCD,
      supportsBreakpoints: true,
    },
    
    // Sensors - Sonar
    get_sonar_distance: {
      text: "%name . %unit ()",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "sonarName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentSonarsForDropdown), name),
        },
        "unit": {
          name: "unit",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("distance_mm"), "mm"],
                                                                           [blocklyTranslate("distance_cm"), "cm"],
                                                                           [blocklyTranslate("distance_m"), "m"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },

    // Sensors - Buttons
    button_check_state: {
      text: "buttons. %state ( %pin )",
      type: types.BOOLEAN,
      inputs: {
        "state": {
          name: "state",
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("isPressed"),"press"],
                                                                           [blocklyTranslate("isReleased"),"release"]]), name),
        },
        "pin": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },
    button_wait_for_action: {
      text: "buttons. %action ( %pin ) ;",
      type: null,
      inputs: {
        "action": {
          name: "action",
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("waitForPress"),"press"],
                                                                           [blocklyTranslate("waitForRelease"),"release"]]), name),
        },
        "pin": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },
    /*
     TODO(Richo): This block is too large when its inputs are inlined (especially in spanish)
     but too ugly when its inputs are external. I don't know how to make it smaller...
     */
    button_wait_for_long_action: {
      text: "buttons . %action ( %pin, %time %timeUnit );",
      type: null,
      inputs: {
        "action": {
          name: "action",
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("waitForPress"),"press"],
                                                                           [blocklyTranslate("waitForRelease"),"release"]]), name),
        },
        "pin": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "time": {
          name: "time",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "timeUnit": {
          name: "unit",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("milliseconds"),"ms"],
                                                                                  [blocklyTranslate("seconds"),"s"],
                                                                                  [blocklyTranslate("minutes"),"m"]]), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },
    /*
     TODO(Richo): This block is useful to react to long presses. It will wait
     for a long press and then return the time passed in milliseconds. The name
     is confusing, though. And the usage is complicated as well. Maybe I should
     simplify it to simply "wait for button hold x seconds" or something like that...
     */
    button_ms_holding: {
      text: "buttons . millisecondsHolding ( %pin )",
      type: types.NUMBER,
      inputs: {
        "pin": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },


    // Sensors - Joystick
    get_joystick_x: {
      text: "%name .x",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "joystickName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },
    get_joystick_y: {
      text: "%name .y",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "joystickName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },
    get_joystick_angle: {
      text: "%name .getAngle()",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "joystickName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },
    get_joystick_magnitude: {
      text: "%name .getMagnitude()",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "joystickName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.SENSORS,
      supportsBreakpoints: true,
    },

    // Strings
    string: {
      text: "'%text'",
      type: types.STRING,
      inputs: {
        "text": {
          name: "text",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput('abc'), name),
        }
      },
      connections: { up: false, down: false, left: true},
      color: colors.STRINGS,
      postload: (block) => block.setInputsInline(true),
      supportsBreakpoints: false,
    },
    string_length: {
      text: "strlen ( %string )",
      type: null,
      inputs: {
        "string": {
          name: "string",
          types: [types.STRING],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.STRINGS,
      supportsBreakpoints: false,
    },

    // Sound
    start_tone: {
      text: "startTone( %tone , %pinNumber ) ;",
      type: null,
      inputs: {
        "tone": {
          name: "tone",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "pinNumber": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.SOUND,
      supportsBreakpoints: true,
    },
    play_tone: {
      text: "playTone( %tone , %pinNumber , %time %unit ) ;",
      type: null,
      inputs: {
        "tone": {
          name: "tone",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "pinNumber": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "time": {
          name: "time",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "unit": {
          name: "unit",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("milliseconds"),"ms"],
                                                                                  [blocklyTranslate("seconds"),"s"],
                                                                                  [blocklyTranslate("minutes"),"m"]]), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.SOUND,
      supportsBreakpoints: true,
    },
    start_note: {
      text: "startTone( %note , %pinNumber ) ;",
      type: null,
      inputs: {
        "note": {
          name: "note",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(getNotes), name),
        },
        "pinNumber": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.SOUND,
      supportsBreakpoints: true,
    },
    play_note: {
      text: "playTone( %note , %pinNumber , %time %unit ) ;",
      type: null,
      inputs: {
        "note": {
          name: "note",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(getNotes), name),
        },
        "pinNumber": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "time": {
          name: "time",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "unit": {
          name: "unit",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("milliseconds"),"ms"],
                                                                                  [blocklyTranslate("seconds"),"s"],
                                                                                  [blocklyTranslate("minutes"),"m"]]), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.SOUND,
      supportsBreakpoints: true,
    },
    stop_tone: {
      text: "stopTone( %pinNumber ) ;",
      type: null,
      inputs: {
        "pinNumber": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.SOUND,
      supportsBreakpoints: true,
    },
    stop_tone_wait: {
      text: "stopToneAndWait( %pinNumber , %time %unit ) ;",
      type: null,
      inputs: {
        "pinNumber": {
          name: "pinNumber",
          types: [types.PIN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "time": {
          name: "time",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "unit": {
          name: "unit",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("milliseconds"),"ms"],
                                                                                  [blocklyTranslate("seconds"),"s"],
                                                                                  [blocklyTranslate("minutes"),"m"]]), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.SOUND,
      supportsBreakpoints: true,
    },

    // Control
    conditional_simple: {
      text: "if %1 { \n %2 }",
      type: null,
      inputs: {
        "1": {
          name: "condition",
          types: [types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "trueBranch",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    conditional_full: {
      text: "if %1 { \n %2 } else { \n %3 }",
      type: null,
      inputs: {
        "1": {
          name: "condition",
          types: [types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "trueBranch",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
        "3": {
          name: "falseBranch",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    forever: {
      text: "forever { \n %1 }",
      type: null,
      inputs: {
        "1": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    repeat: {
      text: "%negate %condition { \n %statements }",
      type: null,
      inputs: {
        "negate": {
          name: "negate",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("while"),"false"],
                                                                                  [blocklyTranslate("until"),"true"]]),
                                                                                 name),
        },
        "condition": {
          name: "condition",
          types: [types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "statements": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    repeat_times: {
      text: "repeat %times { \n %statements }",
      type: null,
      inputs: {
        "times": {
          name: "times",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "statements": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    for: {
      text: "for %1 = %2 to %3 by %4 { \n %5 }",
      type: null,
      inputs: {
        "1": {
          name: "variableName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), name),
        },
        "2": {
          name: "start",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "3": {
          name: "stop",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "4": {
          name: "step",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "5": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    delay: {
      text: "%delay ( %time ) ;",
      type: null,
      inputs: {
        "time": {
          name: "time",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "delay": {
          name: "unit",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("delayMs"),"ms"],
                                                                                  [blocklyTranslate("delayS"),"s"],
                                                                                  [blocklyTranslate("delayM"),"m"]]), name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    wait: {
      text: "%negate %condition ;",
      type: null,
      inputs: {
        "negate": {
          name: "negate",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("while"),"false"],
                                                                           [blocklyTranslate("until"),"true"]]), name),
        },
        "condition": {
          name: "condition",
          types: [types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },
    elapsed_time: {
      text: "%timeUnit",
      type: types.NUMBER,
      inputs: {
        "timeUnit": {
          name: "unit",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("milliseconds"),"ms"],
                                                                                 [blocklyTranslate("seconds"),"s"],
                                                                                 [blocklyTranslate("minutes"),"m"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.CONTROL,
      supportsBreakpoints: true,
    },

    // Math & Logic
    boolean: {
      text: "%boolean",
      type: types.BOOLEAN,
      inputs: {
        "boolean": {
          name: "value",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("true"), "true"],
                                                                                  [blocklyTranslate("false"), "false"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.LOGIC,
      supportsBreakpoints: false,
    },
    boolean_cast: {
      text: "bool ( %1 )",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.LOGIC,
      supportsBreakpoints: false,
    },
    logical_not: {
      text: "! %1",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          name: "value",
          types: [types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.LOGIC,
      supportsBreakpoints: true,
    },
    logical_compare: {
      text: "( %left %logical_compare_op %right )",
      type: types.BOOLEAN,
      inputs: {
        "left": {
          name: "left",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "logical_compare_op": {
          name: "operator",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("=="), "=="],
                                                                                  [blocklyTranslate("!="), "!="],
                                                                                  [blocklyTranslate("<"), "<"],
                                                                                  [blocklyTranslate("<="), "<="],
                                                                                  [blocklyTranslate(">"), ">"],
                                                                                  [blocklyTranslate(">="), ">="]]), name),
        },
        "right": {
          name: "right",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.LOGIC,
      supportsBreakpoints: true,
    },
    logical_operation: {
      text: "( %left %logical_operation_op %right )",
      type: types.BOOLEAN,
      inputs: {
        "left": {
          name: "left",
          types: [types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "logical_operation_op": {
          name: "operator",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("&&"),"and"],
                                                                                  [blocklyTranslate("||"),"or"]]), name),
        },
        "right": {
          name: "right",
          types: [types.BOOLEAN],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.LOGIC,
      supportsBreakpoints: true,
    },
    number: {
      text: "%number",
      type: types.NUMBER,
      inputs: {
        "number": {
          name: "value",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldNumber(0), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: false,
    },
    number_cast: {
      text: "number ( %1 )",
      type: types.NUMBER,
      inputs: {
        "1": {
          name: "value",
          types: allTypes(types.BOOLEAN),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: false,
    },
    number_property: {
      text: "%numProp ( %value )",
      type: types.BOOLEAN,
      inputs: {
        "value": {
          name: "value",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "numProp": {
          name: "property",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown([[blocklyTranslate("isEven"),"even"],
                                                                                  [blocklyTranslate("isOdd"),"odd"],
                                                                                  [blocklyTranslate("isPrime"),"prime"],
                                                                                  [blocklyTranslate("isWhole"),"whole"],
                                                                                  [blocklyTranslate("isPositive"),"positive"],
                                                                                  [blocklyTranslate("isNegative"),"negative"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },
    number_divisibility: {
      text: "isDivisibleBy( %1 , %2 )",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          name: "left",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "right",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },
    number_operation: {
      text: "%operation %number \n",
      type: types.NUMBER,
      inputs: {
        "number": {
          name: "number",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "operation": {
          name: "operator",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("square root"),"sqrt"],
                                                                                  [blocklyTranslate("absolute"),"abs"],
                                                                                  [blocklyTranslate("-"),"negate"],
                                                                                  [blocklyTranslate("ln"),"ln"],
                                                                                  [blocklyTranslate("log10"),"log10"],
                                                                                  [blocklyTranslate("e^"),"exp"],
                                                                                  [blocklyTranslate("10^"),"pow10"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },
    number_trig: {
      text: "%trigOperation %number \n",
      type: types.NUMBER,
      inputs: {
        "number": {
          name: "number",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "trigOperation": {
          name: "operator",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("sin"),"sin"],
                                                                                  [blocklyTranslate("cos"),"cos"],
                                                                                  [blocklyTranslate("tan"),"tan"],
                                                                                  [blocklyTranslate("asin"),"asin"],
                                                                                  [blocklyTranslate("acos"),"acos"],
                                                                                  [blocklyTranslate("atan"),"atan"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },
    math_constant: {
      text: "%constant",
      type: types.NUMBER,
      inputs: {
        "constant": {
          name: "constant",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("3.141592653589793"),"PI"],
                                                                                  [blocklyTranslate("2.718281828459045"),"E"],
                                                                                  [blocklyTranslate("1.61803398875"),"GOLDEN_RATIO"],
                                                                                  [blocklyTranslate("1.4142135623730951"),"SQRT2"],
                                                                                  [blocklyTranslate("0.7071067811865476"),"SQRT1_2"],
                                                                                  [blocklyTranslate("Infinity"),"INFINITY"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: false,
    },
    math_arithmetic: {
      text: "( %left %arithmeticOperator %right )",
      type: types.NUMBER,
      inputs: {
        "left": {
          name: "left",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "arithmeticOperator": {
          name: "operator",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("/"),"DIVIDE"],
                                                                                  [blocklyTranslate("*"),"MULTIPLY"],
                                                                                  [blocklyTranslate("-"),"MINUS"],
                                                                                  [blocklyTranslate("+"),"ADD"],
                                                                                  [blocklyTranslate("**"),"POWER"]]), name),
        },
        "right": {
          name: "right",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },
    number_round: {
      text: "%roundingOperation %number \n",
      type: types.NUMBER,
      inputs: {
        "number": {
          name: "number",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "roundingOperation": {
          name: "operator",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[blocklyTranslate("round"),"round"],
                                                                                  [blocklyTranslate("ceil"),"ceil"],
                                                                                  [blocklyTranslate("floor"),"floor"]]), name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },
    number_modulo: {
      text: "%1 % %2 \n",
      type: types.NUMBER,
      inputs: {
        "1": {
          name: "dividend",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "divisor",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },
    number_constrain: {
      text: "constrain ( %1 , %2 , %3 )",
      type: types.NUMBER,
      inputs: {
        "1": {
          name: "value",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "low",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "3": {
          name: "high",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      postload: (block) => block.setInputsInline(true),
      supportsBreakpoints: true,
    },
    number_between: {
      text: "isBetween ( value: %1 , min: %2 , max: %3 )",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          name: "value",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "low",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "3": {
          name: "high",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      postload: (block) => block.setInputsInline(true),
      supportsBreakpoints: true,
    },
    number_random_int: {
      text: "randomInt( %1, %2 )",
      type: types.NUMBER,
      inputs: {
        "1": {
          name: "from",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "2": {
          name: "to",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      postload: (block) => block.setInputsInline(true),
      supportsBreakpoints: true,
    },
    number_random_float: {
      text: "random()",
      type: types.NUMBER,
      inputs: {},
      connections: { up: false, down: false, left: true },
      color: colors.MATH,
      supportsBreakpoints: true,
    },

    // Variables
    variable: {
      text: "%name",
      type: null,
      inputs: {
        "name": {
          name: "variableName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: false,
    },
    declare_local_variable: {
      text: "var %name = %value ;",
      type: null,
      inputs: {
        "name": {
          name: "variableName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldTextInput("temp"), name),
        },
        "value": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.VARIABLES,
      supportsBreakpoints: false,
    },
    set_variable: {
      text: "%name = %value ;",
      type: null,
      inputs: {
        "name": {
          name: "variableName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), name),
        },
        "value": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    increment_variable: {
      text: "%name += %value ;",
      type: null,
      inputs: {
        "name": {
          name: "variableName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), name),
        },
        "value": {
          name: "value",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },

    // Lists
    list_get: {
      text: "%name . get ( %index )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
        "index": {
          name: "index",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_set: {
      text: "%name . set ( %index , %value ) ;",
      type: null,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
        "index": {
          name: "index",
          types: [types.NUMBER],
          builder: (block, input, name) => block.appendValueInput(name),
        },
        "value": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_push: {
      text: "%name . push ( %value ) ;",
      type: null,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
        "value": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_pop: {
      text: "%name . pop ( ) ;",
      type: null,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_clear: {
      text: "%name . clear ( ) ;",
      type: null,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_random: {
      text: "%name . get_random ( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_count: {
      text: "%name . count ( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_size: {
      text: "%name . size ( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_sum: {
      text: "%name . sum ( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_avg: {
      text: "%name . avg ( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_max: {
      text: "%name . max ( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },
    list_min: {
      text: "%name . min ( )",
      type: types.NUMBER,
      inputs: {
        "name": {
          name: "listName",
          types: null,
          builder: (block, input, name) => input.appendField(new Blockly.FieldDropdown(currentListsForDropdown), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.VARIABLES,
      supportsBreakpoints: true,
    },

    // Procedures
    proc_definition_0args: {
      text: "proc %name () { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.PROCEDURES,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    proc_definition_1args: {
      text: "proc %name ( %arg0 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.PROCEDURES,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    proc_definition_2args: {
      text: "proc %name ( %arg0 , %arg1 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.PROCEDURES,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    proc_definition_3args: {
      text: "proc %name ( %arg0 , %arg1 , %arg2 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "arg2": {
          name: "arg2",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.PROCEDURES,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    proc_definition_4args: {
      text: "proc %name ( %arg0 , %arg1 , %arg2 , %arg3 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "arg2": {
          name: "arg2",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), name),
        },
        "arg3": {
          name: "arg3",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg3"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.PROCEDURES,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    proc_definition_5args: {
      text: "proc %name ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "arg2": {
          name: "arg2",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), name),
        },
        "arg3": {
          name: "arg3",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg3"), name),
        },
        "arg4": {
          name: "arg4",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg4"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.PROCEDURES,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    return: {
      text: "exit ;",
      type: null,
      inputs: {},
      connections: { up: true, down: false, left: false },
      color: colors.PROCEDURES,
      supportsBreakpoints: true,
    },
    proc_call_0args: {
      text: "%procName () ;",
      type: null,
      inputs: {
        "procName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(0)), name),
        },
      },
      connections: { up: true, down: true, left: false },
      color: colors.PROCEDURES,
      postload: (block) => block.setInputsInline(true),
      supportsBreakpoints: true,
    },
    proc_call_1args: {
      text: "%procName ( %arg0 ) ;",
      type: null,
      inputs: {
        "procName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(1)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(1), name), name)),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.PROCEDURES,
      supportsBreakpoints: true,
    },
    proc_call_2args: {
      text: "%procName ( %arg0 , %arg1 ) ;",
      type: null,
      inputs: {
        "procName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(2)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(2), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(2), name), name)),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.PROCEDURES,
      supportsBreakpoints: true,
    },
    proc_call_3args: {
      text: "%procName ( %arg0 , %arg1 , %arg2 ) ;",
      type: null,
      inputs: {
        "procName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(3)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(3), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(3), name), name)),
        },
        "arg2": {
          name: "arg2",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(3), name), name)),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.PROCEDURES,
      supportsBreakpoints: true,
    },
    proc_call_4args: {
      text: "%procName ( %arg0 , %arg1 , %arg2 , %arg3 ) ;",
      type: null,
      inputs: {
        "procName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(4)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(4), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(4), name), name)),
        },
        "arg2": {
          name: "arg2",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(4), name), name)),
        },
        "arg3": {
          name: "arg3",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(4), name), name)),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.PROCEDURES,
      supportsBreakpoints: true,
    },
    proc_call_5args: {
      text: "%procName ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 ) ;",
      type: null,
      inputs: {
        "procName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(5)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(5), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(5), name), name)),
        },
        "arg2": {
          name: "arg2",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(5), name), name)),
        },
        "arg3": {
          name: "arg3",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(5), name), name)),
        },
        "arg4": {
          name: "arg4",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastProcedureName(5), name), name)),
        }
      },
      connections: { up: true, down: true, left: false },
      color: colors.PROCEDURES,
      supportsBreakpoints: true,
    },

    // Functions
    func_definition_0args: {
      text: "func %name () { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.FUNCTIONS,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    func_definition_1args: {
      text: "func %name ( %arg0 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.FUNCTIONS,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    func_definition_2args: {
      text: "func %name ( %arg0 , %arg1 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.FUNCTIONS,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    func_definition_3args: {
      text: "func %name ( %arg0 , %arg1 , %arg2 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "arg2": {
          name: "arg2",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.FUNCTIONS,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    func_definition_4args: {
      text: "func %name ( %arg0 , %arg1 , %arg2 , %arg3 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "arg2": {
          name: "arg2",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), name),
        },
        "arg3": {
          name: "arg3",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg3"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.FUNCTIONS,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    func_definition_5args: {
      text: "func %name ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 ) { \n %stmts }",
      type: null,
      inputs: {
        "name": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), name),
        },
        "arg0": {
          name: "arg0",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), name),
        },
        "arg1": {
          name: "arg1",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), name),
        },
        "arg2": {
          name: "arg2",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), name),
        },
        "arg3": {
          name: "arg3",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg3"), name),
        },
        "arg4": {
          name: "arg4",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg4"), name),
        },
        "stmts": {
          name: "statements",
          types: null,
          builder: (block, input, name) => block.appendStatementInput(name),
        },
      },
      connections: { up: false, down: false, left: false },
      color: colors.FUNCTIONS,
      postload: function (block) {
        if (uziSyntax) { block.setInputsInline(true); }
      },
      isTopLevel: true,
      supportsBreakpoints: false,
    },
    return_value: {
      text: "return %value ;",
      type: null,
      inputs: {
        "value": {
          name: "value",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name),
        }
      },
      connections: { up: true, down: false, left: false },
      color: colors.FUNCTIONS,
      supportsBreakpoints: true,
    },
    func_call_0args: {
      text: "%funcName ()",
      type: null,
      inputs: {
        "funcName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(0)), name),
        },
      },
      connections: { up: false, down: false, left: true },
      color: colors.FUNCTIONS,
      postload: (block) => block.setInputsInline(true),
      supportsBreakpoints: true,
    },
    func_call_1args: {
      text: "%funcName ( %arg0 )",
      type: null,
      inputs: {
        "funcName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(1)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(1), name), name)),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.FUNCTIONS,
      supportsBreakpoints: true,
    },
    func_call_2args: {
      text: "%funcName ( %arg0 , %arg1 )",
      type: null,
      inputs: {
        "funcName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(2)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(2), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(2), name), name)),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.FUNCTIONS,
      supportsBreakpoints: true,
    },
    func_call_3args: {
      text: "%funcName ( %arg0 , %arg1 , %arg2 )",
      type: null,
      inputs: {
        "funcName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(3)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(3), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(3), name), name)),
        },
        "arg2": {
          name: "arg2",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(3), name), name)),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.FUNCTIONS,
      supportsBreakpoints: true,
    },
    func_call_4args: {
      text: "%funcName ( %arg0 , %arg1 , %arg2 , %arg3 )",
      type: null,
      inputs: {
        "funcName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(4)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(4), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(4), name), name)),
        },
        "arg2": {
          name: "arg2",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(4), name), name)),
        },
        "arg3": {
          name: "arg3",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(4), name), name)),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.FUNCTIONS,
      supportsBreakpoints: true,
    },
    func_call_5args: {
      text: "%funcName ( %arg0 , %arg1 , %arg2 , %arg3 , %arg4 )",
      type: null,
      inputs: {
        "funcName": {
          name: "scriptName",
          types: null,
          builder: (block, input, name) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(5)), name),
        },
        "arg0": {
          name: "arg0",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(5), name), name)),
        },
        "arg1": {
          name: "arg1",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(5), name), name)),
        },
        "arg2": {
          name: "arg2",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(5), name), name)),
        },
        "arg3": {
          name: "arg3",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(5), name), name)),
        },
        "arg4": {
          name: "arg4",
          types: allTypes(types.NUMBER),
          builder: (block, input, name) => block.appendValueInput(name)
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldLabel(getArgumentName(getLastFunctionName(5), name), name)),
        }
      },
      connections: { up: false, down: false, left: true },
      color: colors.FUNCTIONS,
      supportsBreakpoints: true,
    },
  }

  function init() {
    Blockly.HSV_SATURATION = 0.6;

    blocklyArea = $("#blocks-editor").get(0);
    blocklyDiv = $("#blockly").get(0);

    initFromSpec();

    i18n.on("change", refreshWorkspace);

    return loadToolbox(false).then(function () {
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
        let categories = toolbox.getElementsByTagName("category");
        for (let i = 0; i < categories.length; i++) {
          let category = categories[i];
          category.setAttribute("name", i18n.translate(category.getAttribute("originalName")));
        }
        let buttons = toolbox.getElementsByTagName("button");
        for (let i = 0; i < buttons.length; i++) {
          let button = buttons[i];
          button.setAttribute("text", i18n.translate(button.getAttribute("originalText")));
        }
        workspace.updateToolbox(toolbox);
        refreshToolbox();
      });

      let readOnlyRestoredScheduled = false; // Flag to avoid scheduling a program restore multiple times
      workspace.addChangeListener(function (evt) {
        if (ignoreChanges) {
          ignoreChanges = false;
          return;
        }
        

        if (evt.type == Blockly.Events.UI) {
          userInteraction = true;

          if (evt.element == "selected") {
            if (selectedBlock != evt.newValue) {
              selectedBlock = evt.newValue;
              trigger("select", evt.newValue);
            }
          } else if (evt.element == "click") {
            if (selectedBlock != evt.blockId) {
              selectedBlock = evt.blockId;
              trigger("select", evt.blockId);
            }
          }

          return;
        }

        if (userInteraction && readOnly) {
          if (!readOnlyRestoredScheduled) {
            readOnlyRestoredScheduled = true;
            setTimeout(() => {
              fromXMLText(readOnlyProgram);
              readOnlyRestoredScheduled = false;
            }, 100);
          }
          return;
        }
        
        /*
        NOTE(Richo): Whenever a block is created or deleted we update the timestamps map.
        These timestamps should help us disambiguate when two proc/func blocks with the same
        name exist and we rename one. What should we do with the calling blocks?
        If we always update the calling blocks then we get into surprising behavior.
        For example, right after we duplicate an existing proc if we rename it then it will
        update calling blocks that we didn't intended to change. Because they refer to the
        original proc, not the new one! Using only the proc name to distinguish them makes
        this impossible. So if we store the creation time of each block we can use it to
        choose the older one, and avoid updating calling blocks when a younger block is
        renamed. It's kind of complicated but I think it works...
        */
        if (evt.type == Blockly.Events.CREATE) {
          let time = +new Date();
          evt.ids.forEach(id => timestamps.set(id, time));
        } else if (evt.type == Blockly.Events.DELETE) {
          evt.ids.forEach(id => timestamps.delete(id));
        }

        if (evt.type == Blockly.Events.MOVE) {
          let block = workspace.getBlockById(evt.blockId);
          if (block != null) {
            block.setDisabled(evt.newParentId == undefined &&
                              !spec[block.type].isTopLevel);
          }
        }


        handleTaskBlocksEvents(evt);
        handleProcedureBlocksEvents(evt);
        handleFunctionBlocksEvents(evt);

        handleVariableDeclarationBlocksEvents(evt);

        trigger("change", userInteraction);
        
        if (userInteraction && !UziCode.isFocused()) {
          let evtType = evt.type;
          let blockType = null;
          let block = workspace.getBlockById(evt.blockId);
          if (block) { blockType = block.type; }
          if (!blockType && evt.xml) { blockType = evt.xml.getAttribute("type"); }
          if (!blockType && evt.oldXml) { blockType = evt.oldXml.getAttribute("type"); }
          Uzi.elog("BLOCKLY/EVENT", {type: evtType, block: blockType});
        }
      });

      initTasksToolboxCategory(workspace);
      initDCMotorsToolboxCategory(workspace);
      initLCDToolboxCategory(workspace);
      initSonarToolboxCategory(workspace);
      initJoystickToolboxCategory(workspace);
      initVariablesToolboxCategory(workspace);
      initListsToolboxCategory(workspace);
      initProceduresToolboxCategory(workspace);
      initFunctionsToolboxCategory(workspace);

      window.addEventListener('resize', resizeBlockly, false);
      resizeBlockly();
    });
  }

  function initTasksToolboxCategory(workspace) {
    let taskDeclaringBlocks = new Set(["task", "timer"]);
    let taskControlBlocks = new Set(["start_task", "stop_task", "resume_task", "pause_task", "run_task"]);

    workspace.registerToolboxCategoryCallback("TASKS", function () {
      let node = XML.getChildNode(toolbox, "Tasks", "originalName");

      // Handle task declaring blocks. Make sure a new name is set by default to avoid collisions
      {
        let blocks = Array.from(node.getElementsByTagName("block"))
          .filter(block => taskDeclaringBlocks.has(block.getAttribute("type")));

        let fields = blocks.map(function (block) {
          return Array.from(block.getElementsByTagName("field"))
            .filter(field => field.getAttribute("name") == "scriptName");
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
          .filter(block => taskControlBlocks.has(block.getAttribute("type")));

        let fields = blocks.map(function (block) {
          return Array.from(block.getElementsByTagName("field"))
            .filter((field) => field.getAttribute("name") == "scriptName");
        }).flat();

        let tasks = getCurrentTaskNames();
        let defaultName = tasks.length > 0 ? tasks[tasks.length-1] : "default";
        fields.forEach(field => field.innerText = defaultName);
      }

      return Array.from(node.children);
    });
  }

  function initDCMotorsToolboxCategory(workspace) {
    workspace.registerToolboxCategoryCallback("DC_MOTORS", function () {
      let node = XML.getChildNode(toolbox, "DC motor", "originalName");
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
  }

  function initLCDToolboxCategory(workspace) {
    workspace.registerToolboxCategoryCallback("LCD", function () {
      let node = XML.getChildNode(toolbox, "LCD", "originalName");
      let nodes = Array.from(node.children);
      if (lcds.length == 0) {
        nodes.splice(1); // Leave the button only
      } else {
        let fields = node.getElementsByTagName("field");
        for (let i = 0; i < fields.length; i++) {
          let field = fields[i];
          if (field.getAttribute("name") === "lcdName") {
            field.innerText = lcds[lcds.length-1].name;
          }
        }
      }
      return nodes;
    });
  }

  function initSonarToolboxCategory(workspace) {
    workspace.registerToolboxCategoryCallback("SONAR", function () {
      let node = XML.getChildNode(toolbox, "Sonar", "originalName");
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
  }

  function initJoystickToolboxCategory(workspace) {
    workspace.registerToolboxCategoryCallback("JOYSTICK", function () {
      let node = XML.getChildNode(toolbox, "Joystick", "originalName");
      let nodes = Array.from(node.children);
      if (joysticks.length == 0) {
        nodes.splice(1); // Leave the button only
      } else {
        let fields = node.getElementsByTagName("field");
        for (let i = 0; i < fields.length; i++) {
          let field = fields[i];
          if (field.getAttribute("name") === "joystickName") {
            field.innerText = joysticks[joysticks.length-1].name;
          }
        }
      }
      return nodes;
    });
  }

  function initVariablesToolboxCategory(workspace) {
    workspace.registerToolboxCategoryCallback("VARIABLES", function () {
      let node = XML.getChildNode(toolbox, "Variables", "originalName");
      let nodes = Array.from(node.children);
      if (variables.length == 0) {
        // Leave the button and declare_local_variable
        nodes = nodes.filter(node => node.nodeName == "button" || node.getAttribute("type") == "declare_local_variable");
      } else {
        let fields = node.getElementsByTagName("field");
        for (let i = 0; i < fields.length; i++) {
          let field = fields[i];
          if (field.getAttribute("name") === "variableName") {
            field.innerText = variables[variables.length-1].name;
          }
        }
      }
      return nodes;
    });
  }

  function initListsToolboxCategory(workspace) {
    workspace.registerToolboxCategoryCallback("LISTS", function () {
      let node = XML.getChildNode(toolbox, "Lists", "originalName");
      let nodes = Array.from(node.children);
      if (lists.length == 0) {
        nodes.splice(1); // Leave the button
      } else {
        let fields = node.getElementsByTagName("field");
        for (let i = 0; i < fields.length; i++) {
          let field = fields[i];
          if (field.getAttribute("name") === "listName") {
            field.innerText = lists[lists.length-1].name;
          }
        }
      }
      return nodes;
    });
  }

  function initProceduresToolboxCategory(workspace) {
    let procDeclaringBlocks = new Set(["proc_definition_0args", "proc_definition_1args",
                                       "proc_definition_2args", "proc_definition_3args",
                                       "proc_definition_4args", "proc_definition_5args"]);
    let procCallingBlocks = ["proc_call_0args", "proc_call_1args", 
                            "proc_call_2args", "proc_call_3args", 
                            "proc_call_4args", "proc_call_5args"];

    workspace.registerToolboxCategoryCallback("PROCEDURES", function () {
      let node = XML.getChildNode(toolbox, "Procedures", "originalName");
      let nodes = Array.from(node.children);

      // Handle proc declaring blocks. Make sure a new name is set by default to avoid collisions
      {
        let blocks = Array.from(node.getElementsByTagName("block"))
          .filter(block => procDeclaringBlocks.has(block.getAttribute("type")));

        let fields = blocks.map(function (block) {
          return Array.from(block.getElementsByTagName("field"))
            .filter(field => field.getAttribute("name") == "scriptName");
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
        procCallingBlocks.forEach(function (type, nargs) {
          let procs = getCurrentProcedureNames(nargs);
          if (procs.length == 0) {
            let index = nodes.findIndex(n => n.getAttribute("type") == type);
            if (index > -1) { nodes.splice(index, 1); }
          } else {
            let defaultName = procs.length > 0 ? procs[procs.length-1] : "default";
            Array.from(node.getElementsByTagName("block"))
              .filter(block => block.getAttribute("type") == type)
              .map(block => Array.from(block.getElementsByTagName("field"))
                  .filter(field => field.getAttribute("name") == "scriptName"))
              .flat()
              .forEach(field => field.innerText = defaultName);
            }
        });
      }

      return nodes;
    });
  }

  function initFunctionsToolboxCategory(workspace) {
    let funcDeclaringBlocks = new Set(["func_definition_0args", "func_definition_1args",
                                       "func_definition_2args", "func_definition_3args",
                                       "func_definition_4args", "func_definition_5args"]);
    let funcCallingBlocks = ["func_call_0args", "func_call_1args", 
                             "func_call_2args", "func_call_3args", 
                             "func_call_4args", "func_call_5args"];

    workspace.registerToolboxCategoryCallback("FUNCTIONS", function () {
      let node = XML.getChildNode(toolbox, "Functions", "originalName");
      let nodes = Array.from(node.children);

      // Handle func declaring blocks. Make sure a new name is set by default to avoid collisions
      {
        let blocks = Array.from(node.getElementsByTagName("block"))
          .filter(block => funcDeclaringBlocks.has(block.getAttribute("type")));

        let fields = blocks.map(function (block) {
          return Array.from(block.getElementsByTagName("field"))
            .filter(field => field.getAttribute("name") == "scriptName");
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
        funcCallingBlocks.forEach(function (type, nargs) {
          let funcs = getCurrentFunctionNames(nargs);
          if (funcs.length == 0) {
            let index = nodes.findIndex(n => n.getAttribute("type") == type);
            if (index > -1) { nodes.splice(index, 1); }
          } else {
            let defaultName = funcs.length > 0 ? funcs[funcs.length-1] : "default";
            Array.from(node.getElementsByTagName("block"))
              .filter(block => block.getAttribute("type") == type)
              .map(block => Array.from(block.getElementsByTagName("field"))
                  .filter(field => field.getAttribute("name") == "scriptName"))
              .flat()
              .forEach(field => field.innerText = defaultName);
            }
        });
      }

      return nodes;
    });
  }

  function initFromSpec() {
    for (let key in spec) {
      let blockSpec = spec[key];
      Blockly.Blocks[key] = {
        customContextMenu: function(options) {
          var option = {
              enabled: blockSpec.supportsBreakpoints && Uzi.state.features["debugging?"],
              text: i18n.translate('Toggle Breakpoint'),
              callback: function(e) {
                try {
                  let block = Blockly.ContextMenu.currentBlock;
                  let token = Uzi.state.program["block->token"][block.id];
                  if (token) {
                    let loc = token[2];
                    if (loc) {
                      Debugger.toggleBreakpoint(loc);
                      if (Debugger.getBreakpoints().has(loc)) {
                        setBreakpointOnBlock(block, loc);
                      } else {
                        clearBreakpointOnBlock(block);
                      }
                    }
                  }
                } catch (err) {
                  console.error(err);
                }
              },
          };
          options.push(option);
        }, 
        init: function () {
          try {
            let msg = blocklyTranslate(blockSpec.text);
            let inputFields = {};
            for (let inputKey in blockSpec.inputs) {
              let inputSpec = blockSpec.inputs[inputKey];
              inputFields[inputKey] = (block, input) => {
                let inputResult = inputSpec.builder(block, input, inputSpec.name);
                if (inputSpec.types) {
                  inputResult.setCheck(inputSpec.types);
                }
                return inputResult;
              }
            }

            // TODO(Richo): If unused remove!
            if (blockSpec.preload) {
              blockSpec.preload(this);
            }

            initBlock(this, msg, inputFields);
            this.setPreviousStatement(blockSpec.connections.up, null);
            this.setNextStatement(blockSpec.connections.down, null);
            this.setOutput(blockSpec.connections.left, blockSpec.type);
            this.setColour(blockSpec.color);
            this.setTooltip(blockSpec.tooltip || "");
            this.setHelpUrl(blockSpec.helpUrl || "");

            // TODO(Richo): If unused remove!
            if (blockSpec.postload) {
              blockSpec.postload(this);
            }
          } catch (err) {
            debugger;
          }
        }
      }
    }
  }

  function initBlock (block, msg, inputFields) {
    //block.contextMenu = false;

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
    let inputFieldRefPattern = /%[^ ]+\b/g;
    let fieldRefMatch;
    let fieldRefName;
    let msgUntilFieldRef;
    let previousRefMatchIndex = 0;
    let placeholders = new Set();
    while((fieldRefMatch = inputFieldRefPattern.exec(msg)) != null) {
        fieldRefName = fieldRefMatch[0].substring(1);
        msgUntilFieldRef = msg.substring(previousRefMatchIndex, fieldRefMatch.index).trim();
        previousRefMatchIndex = inputFieldRefPattern.lastIndex;

        let tempInputName = "___" + fieldRefName + "___";
        let tempInput = block.appendDummyInput(tempInputName);
        let input = inputFields[fieldRefName](block, tempInput);
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
        let msgAfterLastFieldRef = msg.substring(previousRefMatchIndex).trim();
        let input = block.appendDummyInput();
        input.appendField(msgAfterLastFieldRef);
    }

    // merge placeholders into actual inputs
    let current = null;
    let inputs = block.inputList.slice();
    for (let i = inputs.length - 1; i >= 0; i--) {
      let input = inputs[i];
      if (placeholders.has(input)) {
        if (current == null) {
          current = input;
        } else {
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

  function getNotes() {
    return [["B0", "31"], ["C1", "33"], ["C#1", "35"], ["D1", "37"],
           ["D#1", "39"], ["E1", "41"], ["F1", "44"], ["F#1", "46"],
           ["G1", "49"], ["G#1", "52"], ["A1", "55"], ["A#1", "58"],
           ["B1", "62"], ["C2", "65"], ["C#2", "69"], ["D2", "73"],
           ["D#2", "78"], ["E2", "82"], ["F2", "87"], ["F#2", "93"],
           ["G2", "98"], ["G#2", "104"], ["A2", "110"], ["A#2", "117"],
           ["B2", "123"], ["C3", "131"], ["C#3", "139"], ["D3", "147"],
           ["D#3", "156"], ["E3", "165"], ["F3", "175"], ["F#3", "185"],
           ["G3", "196"], ["G#3", "208"], ["A3", "220"], ["A#3", "233"],
           ["B3", "247"], ["C4", "262"], ["C#4", "277"], ["D4", "294"],
           ["D#4", "311"], ["E4", "330"], ["F4", "349"], ["F#4", "370"],
           ["G4", "392"], ["G#4", "415"], ["A4", "440"], ["A#4", "466"],
           ["B4", "494"], ["C5", "523"], ["C#5", "554"], ["D5", "587"],
           ["D#5", "622"], ["E5", "659"], ["F5", "698"], ["F#5", "740"],
           ["G5", "784"], ["G#5", "831"], ["A5", "880"], ["A#5", "932"],
           ["B5", "988"], ["C6", "1047"], ["C#6", "1109"], ["D6", "1175"],
           ["D#6", "1245"], ["E6", "1319"], ["F6", "1397"], ["F#6", "1480"],
           ["G6", "1568"], ["G#6", "1661"], ["A6", "1760"], ["A#6", "1865"],
           ["B6", "1976"], ["C7", "2093"], ["C#7", "2217"], ["D7", "2349"],
           ["D#7", "2489"], ["E7", "2637"], ["F7", "2794"], ["F#7", "2960"],
           ["G7", "3136"], ["G#7", "3322"], ["A7", "3520"], ["A#7", "3729"],
           ["B7", "3951"], ["C8", "4186"], ["C#8", "4435"], ["D8", "4699"],
           ["D#8", "4978"]].map(each => [i18n.translate(each["0"]), each["1"]]);
  }

  function getArgumentName(scriptName, name) {
    let definitionBlock = workspace.getTopBlocks()
                                   .find(b => b.getFieldValue("scriptName") == scriptName);

    if (definitionBlock) {
      let fieldValue = definitionBlock.getFieldValue(name);
      return fieldValue + ":";
    } else {
      return name + ":";
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
    return workspace.getTopBlocks()
      .filter(b => interestingBlocks.includes(b.type))
      .map(b => b.getFieldValue("scriptName"));
  }

  function getCurrentProcedureNames(nargs) {
    let interestingBlocks = ["proc_definition_0args", "proc_definition_1args",
                             "proc_definition_2args", "proc_definition_3args",
                             "proc_definition_4args", "proc_definition_5args"];
    if (nargs != undefined) { interestingBlocks = [interestingBlocks[nargs]]; }
    return workspace.getTopBlocks()
      .filter(b => interestingBlocks.includes(b.type))
      .map(b => b.getFieldValue("scriptName"));
  }

  function getLastProcedureName(nargs) {
    let names = getCurrentProcedureNames(nargs);
    return names.length > 0 ? names[names.length - 1] : "default";
  }

  function getCurrentFunctionNames(nargs) {
    let interestingBlocks = ["func_definition_0args", "func_definition_1args",
                             "func_definition_2args", "func_definition_3args",
                             "func_definition_4args", "func_definition_5args"];
    if (nargs != undefined) { interestingBlocks = [interestingBlocks[nargs]]; }
    return workspace.getTopBlocks()
      .filter(b => interestingBlocks.includes(b.type))
      .map(b => b.getFieldValue("scriptName"));
  }

  function getLastFunctionName(nargs) {
    let names = getCurrentFunctionNames(nargs);
    return names.length > 0 ? names[names.length - 1] : "default";
  }

  function currentTasksForDropdown() {
    let tasks = getCurrentTaskNames();
    if (tasks.length == 0) return [[null, null]];
    return tasks.map(function (name) { return [ name, name ]; });
  }

  function currentProceduresForDropdown(nargs) {
    let procs = getCurrentProcedureNames(nargs);
    if (procs.length == 0) return [[null, null]];
    return procs.map(function (name) { return [ name, name ]; });
  }

  function currentFunctionsForDropdown(nargs) {
    let funcs = getCurrentFunctionNames(nargs);
    if (funcs.length == 0) return [[null, null]];
    return funcs.map(function (name) { return [ name, name ]; });
  }

  function currentMotorsForDropdown() {
    if (motors.length == 0) return [[null, null]];
    return motors.map(function(each) { return [ each.name, each.name ]; });
  }

  function currentLcdsForDropdown() {
    if (lcds.length == 0) return [[null, null]];
    return lcds.map(function(each) { return [ each.name, each.name ]; });
  }

  function currentSonarsForDropdown() {
    if (sonars.length == 0) return [[null, null]];
    return sonars.map(function(each) { return [ each.name, each.name ]; });
  }

  function currentJoysticksForDropdown() {
    if (joysticks.length == 0) return [[null, null]];
    return joysticks.map(function(each) { return [ each.name, each.name ]; });
  }

  function currentVariablesForDropdown() {
    if (variables.length == 0) return [[null, null]];
    return variables.map(function(each) { return [ each.name, each.name ]; });
  }

  function currentListsForDropdown() {
    if (lists.length == 0) return [[null, null]];
    return lists.map(function(each) { return [ each.name, each.name ]; });
  }

  function handleScriptBlocksEvents(evt, definitionBlocks, callBlocks) {
    /*
    NOTE(Richo): I a script is being created by user action make sure to assign
    a unique name to avoid collisions as much as possible.
    */
    if (userInteraction && evt.type == Blockly.Events.CREATE
        && definitionBlocks.includes(evt.xml.getAttribute("type"))) {
      let block = workspace.getBlockById(evt.blockId);
      let name = block.getField("scriptName").getValue();
      if (workspace.getTopBlocks()
          .some(b => b != block && b.type == block.type &&
                    b.getField("scriptName").getValue() == name)) {
        let finalName = name;
        let i = 1;
        let names = getCurrentScriptNames();
        while (names.includes(finalName)) {
          finalName = name + i;
          i++;
        }
        block.getField("scriptName").setValue(finalName);
      }
    }

    /*
    NOTE(Richo): If a script is renamed we want to update all calling blocks.
    And if a calling block is changed to refer to another task we need to update
    its argument names.
    */
    if (evt.type == Blockly.Events.CHANGE
       && evt.element == "field"
       && evt.name == "scriptName") {
      let block = workspace.getBlockById(evt.blockId);
      if (block != undefined && definitionBlocks.includes(block.type)) {
        // A definition block has changed, we need to update calling blocks
        // But only if the block is the oldest of its twins!
        let twinBlocks = workspace.getTopBlocks()
          .filter(b => b.type == block.type)
          .filter(b => {
            let field = b.getField("scriptName");
            return field && field.getValue() == evt.oldValue;
          });
        let time = timestamps.get(block.id);
        if (!twinBlocks.some(b => timestamps.get(b.id) < time)) {
          workspace.getAllBlocks()
            .filter(b => callBlocks.includes(b.type))
            .map(b => b.getField("scriptName"))
            .filter(f => f != undefined && f.getValue() == evt.oldValue)
            .forEach(f => f.setValue(evt.newValue));
        }
      } else if (block != undefined && callBlocks.includes(block.type)) {
        // A calling block has changed, we need to update its argument names
        updateArgumentFields(block);
      }
    }

    // NOTE(Richo): If an argument is renamed we want to update all the calling blocks.
    if (evt.type == Blockly.Events.CHANGE
       && evt.element == "field"
       && evt.name && evt.name.startsWith("arg")) {
      let block = workspace.getBlockById(evt.blockId);
      if (block != undefined && definitionBlocks.includes(block.type)) {
        workspace.getAllBlocks()
          .filter(b => callBlocks.includes(b.type) &&
                      block.getFieldValue("scriptName") == b.getFieldValue("scriptName"))
          .forEach(updateArgumentFields);
      }
    }
  }

  function handleTaskBlocksEvents(evt) {
    let definitionBlocks = ["task", "timer"];
    let callBlocks = ["start_task", "stop_task",
                      "resume_task", "pause_task",
                      "run_task"];
    handleScriptBlocksEvents(evt, definitionBlocks, callBlocks);
  }

  function handleProcedureBlocksEvents(evt) {
    let definitionBlocks = ["proc_definition_0args", "proc_definition_1args",
                            "proc_definition_2args", "proc_definition_3args",
                            "proc_definition_4args", "proc_definition_5args"];
    let callBlocks = ["proc_call_0args", "proc_call_1args",
                      "proc_call_2args", "proc_call_3args",
                      "proc_call_4args", "proc_call_5args"];
    handleScriptBlocksEvents(evt, definitionBlocks, callBlocks);
  }

  function handleFunctionBlocksEvents(evt) {
    let definitionBlocks = ["func_definition_0args", "func_definition_1args",
                            "func_definition_2args", "func_definition_3args",
                            "func_definition_4args", "func_definition_5args"];
    let callBlocks = ["func_call_0args", "func_call_1args",
                      "func_call_2args", "func_call_3args",
                      "func_call_4args", "func_call_5args"];
    handleScriptBlocksEvents(evt, definitionBlocks, callBlocks);
  }

  function handleVariableDeclarationBlocksEvents(evt) {
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
            variables.push({ name: variableName, value: 0 });
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
        {types: ["proc_definition_3args", "func_definition_3args"], fields: ["arg0", "arg1", "arg2"]},
        {types: ["proc_definition_4args", "func_definition_4args"], fields: ["arg0", "arg1", "arg2", "arg3"]},
        {types: ["proc_definition_5args", "func_definition_5args"], fields: ["arg0", "arg1", "arg2", "arg3", "arg4"]}
      ];
      blocks.forEach(function (block) {
        if (evt.type == Blockly.Events.CREATE && block.types.includes(evt.xml.getAttribute("type"))) {
          block.fields.forEach(function (fieldName) {
            let field = XML.getChildNode(evt.xml, fieldName);
            if (field != undefined) {
              let variableName = field.innerText;
              if (!variables.some(function (g) { return g.name == variableName})) {
                variables.push({ name: variableName, value: 0 });
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
        {types: ["proc_definition_4args", "func_definition_4args"], fields: ["arg0", "arg1", "arg2", "arg3"]},
        {types: ["proc_definition_5args", "func_definition_5args"], fields: ["arg0", "arg1", "arg2", "arg3", "arg4"]},
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
      let newVar = {index: nextIndex, name: newName, value: 0};
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

  function trigger(evt, args) {
    observers[evt].forEach(function (fn) {
      try {
        fn(args);
      } catch (err) {
        console.log(err);
      }
    });
  }

  /*
  NOTE(Richo): This function will update the names of the arguments in a calling
  block according to the script being called. This is useful in several cases:
  - When the workspace is loaded from XML (because the field labels are not serialized)
  - When the proc/func being called changes (the user can change the dropdown value)
  - When the argument is renamed in the definition block
  */
  function updateArgumentFields(callBlock) {
    callBlock.inputList.filter(i => i.name.startsWith("arg"))
      .forEach(i => {
        let scriptName = callBlock.getFieldValue("scriptName");
        let inputName = i.name;
        i.fieldRow
          .filter(f => f.class_ == inputName)
          .forEach(f => f.setValue(getArgumentName(scriptName, inputName)));
      });
  }

  function getGeneratedCode(){
    let xml = Blockly.Xml.workspaceToDom(workspace);
    let metadata = {
      motors: motors,
      lcds: lcds,
      sonars: sonars,
      joysticks: joysticks,
      variables: variables,
      lists: lists,
    };
    return BlocksToAST.generate(xml, metadata);
  }

  function refreshWorkspace() {
    fromXMLText(toXMLText());
  }

  function refreshToolbox() {
    workspace.toolbox_.refreshSelection();
  }

  function refreshAll() {
    refreshWorkspace();
    refreshToolbox();
  }

  function cleanUp() {
    workspace.cleanUp();    
    //workspace.scrollCenter();
  }

  function toXML() {
    return Blockly.Xml.workspaceToDom(workspace);
  }

  function toXMLText() {
    return Blockly.Xml.domToText(toXML());
  }

  function fromXML(xml) {
    userInteraction = false;
    ignoreChanges = true;
    workspace.clear();
    Blockly.Xml.domToWorkspace(xml, workspace);

    /*
    HACK(Richo): After the workspace is loaded I run this code to make sure all the
    proc/func calls have their argument labels set correctly.
    I need to do this because Blockly.FieldLabel is not serialized, so the arg names
    are not stored in the XML. And if the blocks are not initialized in the correct
    order some call blocks can't find their definition block at init time.
    Newer versions of Blockly have a Blockly.FieldLabelSerializable class that should
    solve our problem but, unfortunately, upgrading Blockly is harder than it looks
    because it breaks our code in a couple of places (particularly initBlock), so
    for now this is valid workaround.
    */
    workspace.getAllBlocks()
      .filter(b => b.type.includes("_call_"))
      .forEach(updateArgumentFields);
  }

  function fromXMLText(xml) {
    fromXML(Blockly.Xml.textToDom(xml));
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
      proc_definition_4args: ["arg0", "arg1", "arg2", "arg3"],
      proc_definition_5args: ["arg0", "arg1", "arg2", "arg3", "arg4"],
      func_definition_1args: ["arg0"],
      func_definition_2args: ["arg0", "arg1"],
      func_definition_3args: ["arg0", "arg1", "arg2"],
      func_definition_4args: ["arg0", "arg1", "arg2", "arg3"],
      func_definition_5args: ["arg0", "arg1", "arg2", "arg3", "arg4"],
    };
    return (interestingBlocks[block.type] || []).map(f => block.getField(f));
  }

  function handleDebuggerUpdate(state, stackFrameIndex) {
    try {
      setReadOnly(state.debugger.isHalted);

      let breakpoints = new Set(state.debugger.breakpoints);
      workspace.getAllBlocks().forEach(block => {
        // NOTE(Richo): If the loc for this block has a breakpoint set then
        // add the warning text to the block, otherwise remove it.
        // Also, just add one breakpoint for each line (the first). After adding
        // the breakpoint remove it from the set so future blocks don't use it
        let token = Uzi.state.program["block->token"][block.id];
        if (token) {
          let loc = token[2];
          if (breakpoints.has(loc)) {
            breakpoints.delete(loc);
            setBreakpointOnBlock(block, loc);
          } else {
            clearBreakpointOnBlock(block);
          }
        } else {
          clearBreakpointOnBlock(block);
        }
      });

      workspace.highlightBlock(null);
      let blocks = [];
      if (state.debugger.isHalted && state.debugger.stackFrames.length > 0) {
        let stackFrame = state.debugger.stackFrames[stackFrameIndex];
        blocks = stackFrame.blocks.filter(id => id != null); // Only valid blocks please!
      }
      if (blocks.length > 0) {        
        centerOnBlock(blocks[blocks.length - 1]);
        blocks.forEach(id => workspace.highlightBlock(id, true));
      }
    } catch (err) {
      console.log(err);
    }
  }

  function setBreakpointOnBlock(block, loc) {
    let warning = i18n.translate("BREAKPOINT ON LINE: ") + (loc + 1);
    if (block.warning != warning) {
      block.setWarningText(warning);
    }
  }

  function clearBreakpointOnBlock(block) {
    if (block.warning) {
      block.setWarningText(null);
    }
  }

  function centerOnBlock(id) {
    // NOTE(Richo): If we try to move the workspace while a block is being dragged, for some reason,
    // it breaks *everything* and the workspace becomes unusable
    if (workspace.isDragging()) return;

    // NOTE(Richo): Code taken and adapted from
    // https://github.com/google/blockly/issues/1013#issuecomment-290713644
    let block = workspace.getBlockById(id);
    if (block == null) return;

    ignoreChanges = true;
    block.select();      // *block* is the block to scroll into view.
    var mWs = workspace;
    var xy = block.getRelativeToSurfaceXY();	// Scroll the workspace so that the block's top left corner
    var m = mWs.getMetrics();				        	// is in the (0.2; 0.3) part of the viewport.
    mWs.scrollbar.set(
      xy.x * mWs.scale - m.contentLeft - m.viewWidth  * 0.4,
      xy.y * mWs.scale - m.contentTop  - m.viewHeight * 0.4);
  }

  function findBlockByCodeIndex(idx) {
    // TODO(Richo): This implementation sucks!

    let candidates = [];
    Object.entries(Uzi.state.program["block->token"]).forEach(entry => {
      let block = entry[0];
      let t = entry[1];
      if (block != "") {
        if (idx >= t[0] && idx <= t[1]) {
          candidates.push({
            length: t[1] - t[0],
            block: block,
          })
        }
      }
    });
    candidates.sort((a, b) => a.length - b.length);

    let block = null;
    for (let i = 0; i < candidates.length; i++) {
      block = workspace.getBlockById(candidates[i].block);
      if (block != null) break;
    }

    return block;
  }

  function selectByIndex(idx) {
    if (!workspace) return;
    
    let block = findBlockByCodeIndex(idx);
    if (block == null) return;

    ignoreChanges = true;
    block.select();
  }

  function setReadOnly(value) {
    if (readOnly == value) return;
    readOnlyProgram = value ? toXMLText() : null;
    readOnly = value;
  }

  function loadToolbox(advancedFlag) {
    toolboxName = advancedFlag ? 'toolbox-advanced.xml' : 'toolbox-basic.xml';
    return ajax.GET(toolboxName).then(toolboxData => {        
      if (typeof(toolboxData) == "string") {
        toolbox = Blockly.Xml.textToDom(toolboxData);
      } else {
        toolbox = toolboxData.documentElement;
      }
      let categories = toolbox.getElementsByTagName("category");
      for (let i = 0; i < categories.length; i++) {
        let category = categories[i];
        let name = category.getAttribute("name");
        category.setAttribute("originalName", name);
        category.setAttribute("name", i18n.translate(name));

        let color = colors[name.toUpperCase()];
        if (color != undefined) {
          category.setAttribute("colour", color);
        }
      }
      let buttons = toolbox.getElementsByTagName("button");
      for (let i = 0; i < buttons.length; i++) {
        let button = buttons[i];
        button.setAttribute("originalText", button.getAttribute("text"));
        button.setAttribute("text", i18n.translate(button.getAttribute("originalText")));
      }
      return toolbox;
    });
  }

  function setToolbox(advancedFlag) {
    loadToolbox(advancedFlag).then(toolbox => {
      workspace.updateToolbox(toolbox);
      refreshToolbox();
    });
  }

  function getTopBlockName(block) {
    return block.getFieldValue("scriptName");
  }

  function getTopBlocksPositions() {
    // NOTE(Richo): This function returns a map whose keys are each top block's name 
    // and its values are the position of each top block relative to the bottom-left
    // of its previous block (sorted from top to bottom)
    let positions = {};
    let x = 0;
    let y = 0;
    workspace.getTopBlocks().forEach(block => {
      let abs_pos = block.getRelativeToSurfaceXY();
      positions[getTopBlockName(block)] = {
        x: abs_pos.x - x,
        y: abs_pos.y - y,
      };
      x = abs_pos.x;
      y = abs_pos.y + block.height;
    });
    return positions;
  }
  
  function setTopBlocksPositions(positions) {
    // NOTE(Richo): This function restores the positions of all top blocks according to
    // the 'positions' argument, which must be the result or getTopBlocksPositions()
    let x = 0;
    let y = 0;
    workspace.getTopBlocks().forEach(block => {
      let name = getTopBlockName(block);
      if (name in positions) {
        let pos = positions[name];
        x += pos.x;
        y += pos.y;
      } else {
        y += 24;
      }
      block.moveBy(x, y);
      y += block.height;
    });
  }

  return {
    init: init,
    on: on,
    refreshAll: refreshAll,
    refreshToolbox: refreshToolbox,
    resizeWorkspace: resizeBlockly,

    version: version,
    types: types,
    spec: spec,

    setUziSyntax: function (value) {
      uziSyntax = value;
      refreshAll();
    },

    setBasicToolbox: () => setToolbox(false),
    setAdvancedToolbox: () => setToolbox(true),

    cleanUp: cleanUp,

    getGeneratedCode: getGeneratedCode,

    getWorkspace: function () { return workspace; },
    getMotors: function () { return motors; },
    setMotors: function (data) {
      if (readOnly) return;
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
    getLcds: function () { return lcds; },
    setLcds: function (data) {
      if (readOnly) return;
      let renames = new Map();
      data.forEach(function (m) {
        if (lcds[m.index] == undefined) return;
        renames.set(lcds[m.index].name, m.name);
      });

      workspace.getAllBlocks()
        .map(b => ({ block: b, field: b.getField("lcdName") }))
        .filter(o => o.field != undefined)
        .forEach(function (o) {
          let value = renames.get(o.field.getValue());
          if (value == undefined) {
            o.block.dispose(true);
          } else {
            o.field.setValue(value);
          }
        });

      lcds = data;
    },
    getSonars: function () { return sonars; },
    setSonars: function (data) {
      if (readOnly) return;
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
    getJoysticks: function () { return joysticks; },
    setJoysticks: function (data) {
      if (readOnly) return;
      let renames = new Map();
      data.forEach(function (m) {
        if (joysticks[m.index] == undefined) return;
        renames.set(joysticks[m.index].name, m.name);
      });

      workspace.getAllBlocks()
        .map(b => ({ block: b, field: b.getField("joystickName") }))
        .filter(o => o.field != undefined)
        .forEach(function (o) {
          let value = renames.get(o.field.getValue());
          if (value == undefined) {
            o.block.dispose(true);
          } else {
            o.field.setValue(value);
          }
        });

      joysticks = data;
    },
    getVariables: function () { return variables; },
    setVariables: function (data) {
      if (readOnly) return;
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
    getLists: function () { return lists; },
    setLists: function (data) {
      if (readOnly) return;
      let renames = new Map();
      data.forEach(function (m) {
        if (lists[m.index] == undefined) return;
        renames.set(lists[m.index].name, m.name);
      });

      workspace.getAllBlocks()
        .map(b => ({ block: b, field: b.getField("listName") }))
        .filter(o => o.field != undefined)
        .forEach(function (o) {
          let value = renames.get(o.field.getValue());
          if (value == undefined) {
            o.block.dispose(true);
          } else {
            o.field.setValue(value);
          }
        });

      lists = data;
    },
    getProgram: function () {
      return {
        version: version,
        blocks: toXMLText(),
        motors: motors,
        lcds: lcds,
        sonars: sonars,
        joysticks: joysticks,
        variables: variables,
        lists: lists,
      };
    },
    setProgram: function (d) {
      // Check compatibility
      if (d.version != version) { return false; }

      try {
        userInteraction = false;
        fromXMLText(d.blocks);
        motors = d.motors || [];
        lcds = d.lcds || [];
        sonars = d.sonars || [];
        joysticks = d.joysticks || [];
        variables = d.variables || [];
        lists = d.lists || [];
        return true;
      } catch (err) {
        console.log(err);
        return false;
      }
    },

    getTopBlocksPositions: getTopBlocksPositions,
    setTopBlocksPositions: setTopBlocksPositions,

    getUsedVariables: getUsedVariables,
    handleDebuggerUpdate: handleDebuggerUpdate,
    getSelectedBlock: () => selectedBlock,
    selectByIndex: selectByIndex,
    setReadOnly: setReadOnly,
  }
})();
