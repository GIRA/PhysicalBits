let UziBlock = (function () {

  let version = 1;
  let blocklyArea, blocklyDiv, workspace;
  let motors = [];
  let sonars = [];
  let joysticks = [];
  let variables = [];
  let observers = {
    "change" : [],
  };

  const types = {
    ANY: null,
    PIN: "pin",
    NUMBER: "number",
    BOOLEAN: "boolean"
  };

  const spec = {
    // Tasks
    task: {
      text: "task named %1 statements %2",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldTextInput("default"), "taskName")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 175
    },
    timer: {
      text: "timer named %1 running %2 times per %3 with initial state %4 statements %5",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldTextInput("default"), "taskName")
        },
        "2": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldNumber(1000, 0, 999999), "runningTimes")
        },
        "3": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("ticking scale second"),"s"],
                                                                           [i18n.translate("ticking scale minute"),"m"],
                                                                           [i18n.translate("ticking scale hour"),"h"]]), "tickingScale")
        },
        "4": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("running"),"started"],
                                                                           [i18n.translate("stopped"),"stopped"]]), "initialState")
        },
        "5": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 175
    },
    start_task: {
      text: "start task %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), "taskName")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 175
    },
    pause_task: {
      text: "pause task %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), "taskName")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 175
    },
    stop_task: {
      text: "stop task %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), "taskName")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 175
    },
    run_task: {
      text: "run task %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), "taskName")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 175
    },
    resume_task: {
      text: "resume task %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentTasksForDropdown), "taskName")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 175
    },

    // GPIO
    toggle_variable: {
      text: "toggle pin %1",
      type: null,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    turn_pin_variable: {
      text: "set state %1 on pin %2",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("turn state on"), "on"],
                                                                           [i18n.translate("turn state off"), "off"]]),
                                                                           "pinState")
        },
        "2": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    is_pin_variable: {
      text: "is %1 pin %2",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("pin state on"), "on"],
                                                                           [i18n.translate("pin state off"), "off"]]),
                                                                          "pinState")
        },
        "2": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },
    read_pin_variable: {
      text: "read pin %1",
      type: types.NUMBER,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },
    write_pin_variable: {
      text: "set pin %1 to value %2",
      type: null,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
        "2": {
          types: [types.NUMBER, types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("pinValue")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    set_pin_mode: {
      text: "set pin %1 mode to %2",
      type: null,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
        "2": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("INPUT"),"INPUT"],
                                                                                  [i18n.translate("OUTPUT"),"OUTPUT"],
                                                                                  [i18n.translate("INPUT PULLUP"),"INPUT_PULLUP"]]), "mode")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    pin: {
      text: "pin %pin",
      type: types.PIN,
      inputs: {
        "pin": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                         .appendField(new Blockly.FieldDropdown([["D0","D0"], ["D1","D1"], ["D2","D2"], ["D3","D3"],
                                                                                 ["D4","D4"], ["D5","D5"], ["D6","D6"], ["D7","D7"],
                                                                                 ["D8","D8"], ["D9","D9"], ["D10","D10"], ["D11","D11"],
                                                                                 ["D12","D12"], ["D13","D13"], ["A0","A0"], ["A1","A1"],
                                                                                 ["A2","A2"], ["A3","A3"], ["A4","A4"], ["A5","A5"]]),
                                                                                "pinNumber")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },
    pin_cast: {
      text: "pin cast %1",
      type: types.PIN,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => block.appendValueInput("value")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },

    // Motors - Servo
    set_servo_degrees: {
      text: "set degrees of servo on pin %1 to %2",
      type: null,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("servoValue")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    get_servo_degrees: {
      text: "get degrees of servo on pin %1",
      type: types.NUMBER,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },

    // Motors - DC
    move_dcmotor: {
      text: "move dcmotor %name in %direction at speed %speed",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName")
        },
        "direction": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("forward"),"fwd"],
                                                                           [i18n.translate("backward"),"bwd"]]), "direction")
        },
        "speed": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("speed")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    stop_dcmotor: {
      text: "stop dcmotor %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    change_speed_dcmotor: {
      text: "set dcmotor %name speed to %speed",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName")
        },
        "speed": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("speed")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    get_speed_dcmotor: {
      text: "get dcmotor %name speed",
      type: types.NUMBER,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentMotorsForDropdown), "motorName")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },

    // Sensors - Sonar
    get_sonar_distance: {
      text: "read distance from sonar %name in units %unit",
      type: types.NUMBER,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentSonarsForDropdown), "sonarName")
        },
        "unit": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("distance_mm"), "mm"],
                                                                           [i18n.translate("distance_cm"), "cm"],
                                                                           [i18n.translate("distance_m"), "m"]]), "unit")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },

    // Sensors - Buttons
    button_check_state: {
      text: "is button %state on pin %pin",
      type: types.BOOLEAN,
      inputs: {
        "state": {
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("button pressed"),"press"],
                                                                           [i18n.translate("button released"),"release"]]), "state")
        },
        "pin": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },
    button_wait_for_action: {
      text: "wait for button %action on pin %pin",
      type: null,
      inputs: {
        "action": {
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("button waitForPress"),"press"],
                                                                           [i18n.translate("button waitForRelease"),"release"]]), "action")
        },
        "pin": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    /*
     TODO(Richo): This block is too large when its inputs are inlined (especially in spanish)
     but too ugly when its inputs are external. I don't know how to make it smaller...
     */
    button_wait_for_long_action: {
      text: "wait button %action on pin %pin for %time %timeUnit",
      type: null,
      inputs: {
        "action": {
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("button waitForPress"),"press"],
                                                                           [i18n.translate("button waitForRelease"),"release"]]), "action")
        },
        "pin": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
        "time": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("time")
        },
        "timeUnit": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("milliseconds"),"ms"],
                                                                                  [i18n.translate("seconds"),"s"],
                                                                                  [i18n.translate("minutes"),"m"]]), "unit")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    /*
     TODO(Richo): This block is useful to react to long presses. It will wait
     for a long press and then return the time passed in milliseconds. The name
     is confusing, though. And the usage is complicated as well. Maybe I should
     simplify it to simply "wait for button hold x seconds" or something like that...
     */
    button_ms_holding: {
      text: "elapsed milliseconds while pressing %pin",
      type: types.NUMBER,
      inputs: {
        "pin": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },


    // Sensors - Joystick
    get_joystick_x: {
      text: "read joystick x position from %name",
      type: types.NUMBER,
      inputs: {
        "name": {
          types: null,
          builder: input => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), "joystickName"),
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },
    get_joystick_y: {
      text: "read joystick y position from %name",
      type: types.NUMBER,
      inputs: {
        "name": {
          types: null,
          builder: input => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), "joystickName"),
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },
    get_joystick_angle: {
      text: "read joystick angle from %name",
      type: types.NUMBER,
      inputs: {
        "name": {
          types: null,
          builder: input => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), "joystickName"),
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },
    get_joystick_magnitude: {
      text: "read joystick magnitude from %name",
      type: types.NUMBER,
      inputs: {
        "name": {
          types: null,
          builder: input => input.appendField(new Blockly.FieldDropdown(currentJoysticksForDropdown), "joystickName"),
        }
      },
      connections: { up: false, down: false, left: true },
      color: 0
    },

    // Sound
    start_tone: {
      text: "play tone %1 on pin %2",
      type: null,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("tone")
        },
        "2": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    play_tone: {
      text: "play tone %1 on pin %2 for %3 %4",
      type: null,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("tone")
        },
        "2": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
        "3": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("time")
        },
        "4": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("milliseconds"),"ms"],
                                                                                  [i18n.translate("seconds"),"s"],
                                                                                  [i18n.translate("minutes"),"m"]]), "unit")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    start_note: {
      text: "play note %1 on pin %2",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown(getNotes), "note")
        },
        "2": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    play_note: {
      text: "play note %1 on pin %2 for %3 %4",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown(getNotes), "note")
        },
        "2": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
        "3": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("time")
        },
        "4": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("milliseconds"),"ms"],
                                                                                  [i18n.translate("seconds"),"s"],
                                                                                  [i18n.translate("minutes"),"m"]]), "unit")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    stop_tone: {
      text: "silence pin %1",
      type: null,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },
    stop_tone_wait: {
      text: "silence pin %1 and wait %2 %3",
      type: null,
      inputs: {
        "1": {
          types: [types.PIN],
          builder: (input, block) => block.appendValueInput("pinNumber")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("time")
        },
        "3": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("milliseconds"),"ms"],
                                                                                  [i18n.translate("seconds"),"s"],
                                                                                  [i18n.translate("minutes"),"m"]]), "unit")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 0
    },

    // Control
    boolean: {
      text: "boolean %value",
      type: types.BOOLEAN,
      inputs: {
        "value": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("true"), "true"],
                                                                                  [i18n.translate("false"), "false"]]), "value")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 210
    },
    boolean_cast: {
      text: "boolean cast %1",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => block.appendValueInput("value")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 210
    },
    conditional_simple: {
      text: "if %1 then %2",
      type: null,
      inputs: {
        "1": {
          types: [types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("condition")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendStatementInput("trueBranch")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    conditional_full: {
      text: "if %1 then %2 else %3",
      type: null,
      inputs: {
        "1": {
          types: [types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("condition")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendStatementInput("trueBranch")
        },
        "3": {
          types: null,
          builder: (input, block) => block.appendStatementInput("falseBranch")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    forever: {
      text: "repeat forever \n %1",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    repeat: {
      text: "repeat %1 mode %2 condition %3 statements",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("while"),"false"],
                                                                                  [i18n.translate("until"),"true"]]),
                                                                                 "negate")
        },
        "2": {
          types: [types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("condition")
        },
        "3": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    repeat_times: {
      text: "repeat %1 times \n %2",
      type: null,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("times")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    for: {
      text: "count with %1 from %2 to %3 by %4 %5",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("start")
        },
        "3": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("stop")
        },
        "4": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("step")
        },
        "5": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    delay: {
      text: "delay %1 %2",
      type: null,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("time")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("delay in milliseconds"),"ms"],
                                                                                  [i18n.translate("delay in seconds"),"s"],
                                                                                  [i18n.translate("delay in minutes"),"m"]]), "unit")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    wait: {
      text: "wait %1 %2",
      type: null,
      inputs: {
        "1": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("while"),"false"],
                                                                           [i18n.translate("until"),"true"]]), "negate")
        },
        "2": {
          types: [types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("condition")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 210
    },
    elapsed_time: {
      text: "elapsed time since bootup in %timeUnit",
      type: types.NUMBER,
      inputs: {
        "timeUnit": {
          types: null,
          builder: (input, block) => input.appendField(new Blockly.FieldDropdown([[i18n.translate("milliseconds"),"ms"],
                                                                                 [i18n.translate("seconds"),"s"],
                                                                                 [i18n.translate("minutes"),"m"]]), "unit")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 210
    },
    logical_compare: {
      text: "logical comparison %1 left %2 operator %3 right",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("left")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("logical operator ="), "=="],
                                                                                  [i18n.translate("logical operator ≠"), "!="],
                                                                                  [i18n.translate("logical operator <"), "<"],
                                                                                  [i18n.translate("logical operator ≤"), "<="],
                                                                                  [i18n.translate("logical operator >"), ">"],
                                                                                  [i18n.translate("logical operator ≥"), ">="]]), "operator")
        },
        "3": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("right")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 210
    },
    logical_operation: {
      text: "logical operation %1 left %2 operator %3 right",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: [types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("left")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("logical and"),"and"],
                                                                                  [i18n.translate("logical or"),"or"]]), "operator")
        },
        "3": {
          types: [types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("right")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 210
    },
    logical_not: {
      text: "logical not %1",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: [types.BOOLEAN],
          builder: (input, block) => block.appendValueInput("value")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 210
    },

    // Math
    number: {
      text: "number %1",
      type: types.NUMBER,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldNumber(0), "value")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_cast: {
      text: "number cast %1",
      type: types.NUMBER,
      inputs: {
        "1": {
          types: null,
          builder: (input, block) => block.appendValueInput("value")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_property: {
      text: "number property %1 value %2 property",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("value")
        },
        "2": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("is even"),"even"],
                                                                                  [i18n.translate("is odd"),"odd"],
                                                                                  [i18n.translate("is prime"),"prime"],
                                                                                  [i18n.translate("is whole"),"whole"],
                                                                                  [i18n.translate("is positive"),"positive"],
                                                                                  [i18n.translate("is negative"),"negative"]]), "property")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_divisibility: {
      text: "number %1 is divisible by number %2",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("left")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("right")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_operation: {
      text: "perform %operation on %number",
      type: types.NUMBER,
      inputs: {
        "number": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("number")
        },
        "operation": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("square root"),"sqrt"],
                                                                                  [i18n.translate("absolute"),"abs"],
                                                                                  [i18n.translate("-"),"negate"],
                                                                                  [i18n.translate("ln"),"ln"],
                                                                                  [i18n.translate("log10"),"log10"],
                                                                                  [i18n.translate("e^"),"exp"],
                                                                                  [i18n.translate("10^"),"pow10"]]), "operator")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_trig: {
      text: "perform trigonometric %operation on %number",
      type: types.NUMBER,
      inputs: {
        "number": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("number")
        },
        "operation": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("sin"),"sin"],
                                                                                  [i18n.translate("cos"),"cos"],
                                                                                  [i18n.translate("tan"),"tan"],
                                                                                  [i18n.translate("asin"),"asin"],
                                                                                  [i18n.translate("acos"),"acos"],
                                                                                  [i18n.translate("atan"),"atan"]]), "operator")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    math_constant: {
      text: "math %constant",
      type: types.NUMBER,
      inputs: {
        "constant": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("constant π"),"PI"],
                                                                                  [i18n.translate("constant ℯ"),"E"],
                                                                                  [i18n.translate("constant φ"),"GOLDEN_RATIO"],
                                                                                  [i18n.translate("constant √2"),"SQRT2"],
                                                                                  [i18n.translate("constant √½"),"SQRT1_2"],
                                                                                  [i18n.translate("constant ∞"),"INFINITY"]]), "constant")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    math_arithmetic: {
      text: "arithmetic function %left %operator %right",
      type: types.NUMBER,
      inputs: {
        "left": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("left")
        },
        "operator": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("arithmetic operator /"),"DIVIDE"],
                                                                                  [i18n.translate("arithmetic operator *"),"MULTIPLY"],
                                                                                  [i18n.translate("arithmetic operator -"),"MINUS"],
                                                                                  [i18n.translate("arithmetic operator +"),"ADD"],
                                                                                  [i18n.translate("arithmetic operator ^"),"POWER"]]), "operator"),
        },
        "right": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("right")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_round: {
      text: "perform rounding %operation on %number",
      type: types.NUMBER,
      inputs: {
        "number": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("number")
        },
        "operation": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown([[i18n.translate("round"),"round"],
                                                                                  [i18n.translate("round up"),"ceil"],
                                                                                  [i18n.translate("round down"),"floor"]]), "operator")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_modulo: {
      text: "remainder of %1 ÷ %2",
      type: types.NUMBER,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("dividend")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("divisor")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 225
    },
    number_constrain: {
      text: "constrain %1 low %2 high %3",
      type: types.NUMBER,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("value")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("low")
        },
        "3": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("high")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 225,
      postload: (block) => block.setInputsInline(true)
    },
    number_between: {
      text: "is %1 between %2 and %3",
      type: types.BOOLEAN,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("value")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("low")
        },
        "3": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("high")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 225,
      postload: (block) => block.setInputsInline(true)
    },
    number_random_int: {
      text: "random integer from %1 to %2",
      type: types.NUMBER,
      inputs: {
        "1": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("from")
        },
        "2": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("to")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 225,
      postload: (block) => block.setInputsInline(true)
    },
    number_random_float: {
      text: "random fraction",
      type: types.NUMBER,
      inputs: {},
      connections: { up: false, down: false, left: true },
      color: 225,
    },

    // Variables
    variable: {
      text: "variable %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 330,
    },
    declare_local_variable: {
      text: "declare local variable %name with %value",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName")
        },
        "value": {
          types: null,
          builder: (input, block) => block.appendValueInput("value")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 330
    },
    set_variable: {
      text: "set variable %name to value %value",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName")
        },
        "value": {
          types: null,
          builder: (input, block) => block.appendValueInput("value")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 330
    },
    increment_variable: {
      text: "increment variable %name value by %value",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input) => input.appendField(new Blockly.FieldDropdown(currentVariablesForDropdown), "variableName")
        },
        "value": {
          types: [types.NUMBER],
          builder: (input, block) => block.appendValueInput("value")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 330
    },

    // Procedures
    proc_definition_0args: {
      text: "procedure named %name %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "procName")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 285
    },
    proc_definition_1args: {
      text: "procedure named %name with argument %arg0 %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "procName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), "arg0")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 285
    },
    proc_definition_2args: {
      text: "procedure named %name with arguments %arg0 %arg1 %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "procName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), "arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), "arg1")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 285
    },
    proc_definition_3args: {
      text: "procedure named %name with arguments %arg0 %arg1 %arg2 %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "procName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), "arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), "arg1")
        },
        "arg2": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), "arg2")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 285
    },
    return: {
      text: "procedure exit e.g. return with no value",
      type: null,
      inputs: {},
      connections: { up: true, down: false, left: false },
      color: 285
    },
    proc_call_0args: {
      text: "execute procedure %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(0)), "procName")
        },
      },
      connections: { up: true, down: true, left: false },
      color: 285
    },
    proc_call_1args: {
      text: "execute procedure %name with %arg0",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(1)), "procName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg0")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg0")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 285
    },
    proc_call_2args: {
      text: "execute procedure %name with %arg0 and %arg1",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(2)), "procName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg0")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg1")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg1")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 285
    },
    proc_call_3args: {
      text: "execute procedure %name with %arg0, %arg1 and %arg2",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentProceduresForDropdown(3)), "procName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg0")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg1")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg1")
        },
        "arg2": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg2")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg2")
        }
      },
      connections: { up: true, down: true, left: false },
      color: 285
    },

    // Functions
    func_definition_0args: {
      text: "function named %name %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "funcName")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 265
    },
    func_definition_1args: {
      text: "function named %name with argument %arg0 %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "funcName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), "arg0")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 265
    },
    func_definition_2args: {
      text: "function named %name with arguments %arg0 %arg1 %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "funcName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), "arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), "arg1")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 265
    },
    func_definition_3args: {
      text: "function named %name with arguments %arg0 %arg1 %arg2 %stmts",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldTextInput("default"), "funcName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg0"), "arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg1"), "arg1")
        },
        "arg2": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField(new Blockly.FieldTextInput("arg2"), "arg2")
        },
        "stmts": {
          types: null,
          builder: (input, block) => block.appendStatementInput("statements")
        },
      },
      connections: { up: false, down: false, left: false },
      color: 265
    },
    return_value: {
      text: "function return with value %value",
      type: null,
      inputs: {
        "value": {
          types: null,
          builder: (input, block) => block.appendValueInput("value")
        }
      },
      connections: { up: true, down: false, left: false },
      color: 265
    },
    func_call_0args: {
      text: "evaluate function %name",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(0)), "funcName")
        },
      },
      connections: { up: false, down: false, left: true },
      color: 265
    },
    func_call_1args: {
      text: "evaluate function %name with argument %arg0",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(1)), "funcName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg0")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg0")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 265
    },
    func_call_2args: {
      text: "evaluate function %name with arguments %arg0 %arg1",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(2)), "funcName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg0")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg1")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg1")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 265
    },
    func_call_3args: {
      text: "evaluate function %name with arguments %arg0 %arg1 %arg2",
      type: null,
      inputs: {
        "name": {
          types: null,
          builder: (input, block) => block.appendDummyInput()
                                          .appendField(new Blockly.FieldDropdown(() => currentFunctionsForDropdown(3)), "funcName")
        },
        "arg0": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg0")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg0")
        },
        "arg1": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg1")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg1")
        },
        "arg2": {
          types: null,
          builder: (input, block) => block.appendValueInput("arg2")
                                          .setAlign(Blockly.ALIGN_RIGHT)
                                          .appendField("arg2")
        }
      },
      connections: { up: false, down: false, left: true },
      color: 265
    },
  }

  function init() {
    blocklyArea = $("#blocks-editor").get(0);
    blocklyDiv = $("#blockly").get(0);

    initFromSpec();

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

      workspace.registerToolboxCategoryCallback("JOYSTICK", function () {
        let node = XML.getChildNode(XML.getChildNode(toolbox, "Sensors", "originalName"), "Joystick", "originalName");
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

  function initFromSpec() {
    let typeMap = {};
    typeMap[types.PIN] = "Pin";
    typeMap[types.NUMBER] = "Number";
    typeMap[types.BOOLEAN] = "Boolean";

    for (let key in spec) {
      let blockSpec = spec[key];
      Blockly.Blocks[key] = {
        init: function () {
          try {
            let msg = i18n.translate(blockSpec.text);
            let inputFields = {};
            for (let inputKey in blockSpec.inputs) {
              let inputSpec = blockSpec.inputs[inputKey];
              inputFields[inputKey] = (input, block) => {
                let inputResult = inputSpec.builder(input, block);
                if (inputSpec.types) {
                  inputResult.setCheck(inputSpec.types.map(t => typeMap[t]));
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
            this.setOutput(blockSpec.connections.left, typeMap[blockSpec.type]);
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
        msgUntilFieldRef = trim(msg.substring(previousRefMatchIndex, fieldRefMatch.index));
        previousRefMatchIndex = inputFieldRefPattern.lastIndex;

        let tempInputName = "___" + fieldRefName + "___";
        let tempInput = block.appendDummyInput(tempInputName);
        let input = inputFields[fieldRefName](tempInput, block);
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

  function currentJoysticksForDropdown() {
    if (joysticks.length == 0) return [["", ""]];
    return joysticks.map(function(each) { return [ each.name, each.name ]; });
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
    return BlocksToAST.generate(xml, motors, sonars, joysticks);
  }

  function refreshWorkspace() {
    fromXMLText(toXMLText());
  }

  function refreshToolbox() {
    workspace.toolbox_.refreshSelection();
  }

  function toXML() {
    return Blockly.Xml.workspaceToDom(workspace);
  }

  function toXMLText() {
    return Blockly.Xml.domToText(toXML());
  }

  function updatePositions(xml) {
    for (let i = 0; i < xml.childElementCount; i++) {
      let node = xml.children[i];
      let block = workspace.getTopBlocks().find(b => b.id == node.getAttribute("id"));
      if (block) {
        let position = block.getRelativeToSurfaceXY();
        node.setAttribute("x", position.x);
        node.setAttribute("y", position.y);
      }
    }
  }

  function fromXML(xml, keepPositions) {
    if (keepPositions) {
      updatePositions(xml);
    }
    workspace.clear();
    Blockly.Xml.domToWorkspace(xml, workspace);
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
    //toXMLText: toXMLText,
    //fromXMLText: fromXMLText,

    fromXML: fromXML,
    toXML: toXML,

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
    getJoysticks: function () { return joysticks; },
    setJoysticks: function (data) {
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
        blocks: toXMLText(),
        motors: motors,
        sonars: sonars,
        joysticks: joysticks,
        variables: variables,
      };
    },
    setDataFromStorage: function (d) {
      // Check compatibility
      if (d.version != version) { return; }

      fromXMLText(d.blocks);
      motors = d.motors || [];
      sonars = d.sonars || [];
      joysticks = d.joysticks || [];
      variables = d.variables || [];
    },
    getUsedVariables: getUsedVariables,
  }
})();
