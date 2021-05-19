let ASTToBlocks = (function () {

	let dispatchTable = {
		UziProgramNode: function (json, ctx) {
			let node = create("xml");
			json.imports.forEach(function (imp) {
				let xmlImport = generateXMLFor(imp, ctx);
				if (xmlImport) {
					node.appendChild(xmlImport);
				}
			});
			json.globals.forEach(function (global) {
				ctx.addVariable({
					name: global.name,
					value: getLiteralValue(global.value),
				});
			});
			json.scripts.forEach(function (script) {
				node.appendChild(generateXMLForScript(script, ctx));
			});
			return node;
		},
		UziImportNode: function (json, ctx) {
			function getVariableDefaultValue(varName) {
				let assignment = json.initializationBlock.statements.find(stmt =>
					stmt.__class__ == "UziAssignmentNode" &&
					stmt.left.__class__ == "UziVariableNode" &&
					stmt.left.name == varName);
				if (!assignment) return "0";

				return getLiteralValue(assignment.right);
			}

			// TODO(Richo): Preserve other initializationBlock statements

			if (json.path == "DCMotor.uzi") {
				let name = json.alias;
				let enable = getVariableDefaultValue("enablePin");
				let fwd = getVariableDefaultValue("forwardPin");
				let bwd = getVariableDefaultValue("reversePin");
				ctx.addMotor({
					name: name,
					enable: enable,
					fwd: fwd,
					bwd: bwd
				});
			} else if (json.path == "Sonar.uzi") {
				let name = json.alias;
				let trig = getVariableDefaultValue("trigPin");
				let echo = getVariableDefaultValue("echoPin");
				let maxDist = getVariableDefaultValue("maxDistance");
				// TODO(Richo): Handle start/stop reading
				ctx.addSonar({
					name: name,
					trig: trig,
					echo: echo,
					maxDist: maxDist
				});
			} else if (json.path == "Joystick.uzi") {
				let name = json.alias;
				let xPin = getVariableDefaultValue("xPin");
				let yPin = getVariableDefaultValue("yPin");
				// TODO(Richo): Handle start/stop reading
				ctx.addJoystick({
					name: name,
					xPin: xPin,
					yPin: yPin,
				});
			} else if (json.path == "Buttons.uzi") {
				// HACK(Richo): Do nothing?
			} else if (json.path == "List.uzi") {
				let name = json.alias;
				let size = getVariableDefaultValue("size");
				ctx.addList({
					name: name,
					size: size
				});
			} else {
				return createImportBlock(json, ctx);
			}
		},
		UziYieldNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "yield");
			return node;
		},
		UziTaskNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", json.state === "once" ? "task" : "timer");
			appendField(node, "scriptName", json.name);
			if (json.tickingRate !== null) {
				appendField(node, "runningTimes", json.tickingRate.value);
				appendField(node, "tickingScale", json.tickingRate.scale);
			}
			if (json.state !== "once") {
				appendField(node, "initialState", json.state === "running" ? "started" : "stopped");
			}
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziProcedureNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			let types = ["proc_definition_0args", "proc_definition_1args",
									"proc_definition_2args", "proc_definition_3args"];
			if (json.arguments.length > 3) {
				throw "Max number of arguments for procedure blocks is 3";
			}
			node.setAttribute("type", types[json.arguments.length]);
			let script = ctx.scriptNamed(json.name);
			script.arguments.forEach(function (arg, i) {
				appendField(node, "arg" + i, arg.name);
			});
			appendField(node, "scriptName", json.name);
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziFunctionNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			let types = ["func_definition_0args", "func_definition_1args",
									"func_definition_2args", "func_definition_3args"];
			if (json.arguments.length > 3) {
				throw "Max number of arguments for function blocks is 3";
			}
			node.setAttribute("type", types[json.arguments.length]);
			let script = ctx.scriptNamed(json.name);
			script.arguments.forEach(function (arg, i) {
				appendField(node, "arg" + i, arg.name);
			});
			appendField(node, "scriptName", json.name);
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziCallNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			if (ctx.isTask(json.selector)) {
				node.setAttribute("type", "run_task");
				appendField(node, "scriptName", json.selector);
			} else if (ctx.isProcedure(json.selector) || ctx.isFunction(json.selector)) {
				if (ctx.isInStatementPosition(json)) {
					initProcedureCall(node, json, ctx);
				} else {
					initFunctionCall(node, json, ctx);
				}
			} else if (json.selector.includes(".")){
				initExternalCall(node, json, ctx);
			} else {
				initPrimitiveCall(node, json, ctx);
			}
			return node;
		},
		UziNumberLiteralNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			if (json.value == Math.PI) {
				node.setAttribute("type", "math_constant");
				appendField(node, "constant", "PI");
			} else if (json.value == Math.E) {
				node.setAttribute("type", "math_constant");
				appendField(node, "constant", "E");
			} else if (json.value == Math.SQRT2) {
				node.setAttribute("type", "math_constant");
				appendField(node, "constant", "SQRT2");
			} else if (json.value == Math.SQRT1_2) {
				node.setAttribute("type", "math_constant");
				appendField(node, "constant", "SQRT1_2");
			} else if (json.value == Infinity) {
				node.setAttribute("type", "math_constant");
				appendField(node, "constant", "INFINITY");
			} else if (json.value == 1.61803398875) {
				node.setAttribute("type", "math_constant");
				appendField(node, "constant", "GOLDEN_RATIO");
			} else {
				node.setAttribute("type", "number");
				appendField(node, "value", json.value);
			}
			return node;
		},
		UziPinLiteralNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "pin");
			appendField(node, "pinNumber", json.type + json.number);
			return node;
		},
		UziScriptStartNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "start_task");
			appendField(node, "scriptName", json.scripts[0]);
			return node;
		},
		UziScriptStopNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "stop_task");
			appendField(node, "scriptName", json.scripts[0]);
			return node;
		},
		UziScriptResumeNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "resume_task");
			appendField(node, "scriptName", json.scripts[0]);
			return node;
		},
		UziScriptPauseNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "pause_task");
			appendField(node, "scriptName", json.scripts[0]);
			return node;
		},
		UziConditionalNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type",
				json.falseBranch.statements.length > 0 ?
				"conditional_full" : "conditional_simple");
			appendValue(node, "condition", json.condition, ctx);
			appendStatements(node, "trueBranch", json.trueBranch, ctx);
			appendStatements(node, "falseBranch", json.falseBranch, ctx);
			return node;
		},
		UziForeverNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "forever");
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziRepeatNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "repeat_times");
			appendValue(node, "times", json.times, ctx);
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziWhileNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "repeat");
			appendField(node, "negate", json.negated);
			appendValue(node, "condition", json.condition, ctx);
			appendStatements(node, "statements", json.post, ctx);
			return node;
		},
		UziUntilNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "repeat");
			appendField(node, "negate", json.negated);
			appendValue(node, "condition", json.condition, ctx);
			appendStatements(node, "statements", json.post, ctx);
			return node;
		},
		UziForNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "for");
			appendField(node, "variableName", json.counter.name);
			appendValue(node, "start", json.start, ctx);
			appendValue(node, "stop", json.stop, ctx);
			appendValue(node, "step", json.step, ctx);
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziAssignmentNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			// HACK(Richo): Check if the assignment represents a variable increment
			if (json.right.__class__ === "UziCallNode"
				&& json.right.selector === "+"
				&& json.right.arguments[0].value.__class__ === "UziVariableNode"
				&& json.right.arguments[0].value.name == json.left.name) {
				node.setAttribute("type", "increment_variable");
				appendField(node, "variableName", json.left.name);
				appendValue(node, "value", json.right.arguments[1].value, ctx);
			} else {
				node.setAttribute("type", "set_variable");
				appendField(node, "variableName", json.left.name);
				appendValue(node, "value", json.right, ctx);
			}
			return node;
		},
		UziVariableNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			if (json.name.includes(".")) {
				initExternalVariable(node, json, ctx);
			} else {
				initRegularVariable(node, json, ctx);
			}
			return node;
		},
		UziVariableDeclarationNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "declare_local_variable");
			appendField(node, "variableName", json.name);
			appendValue(node, "value", json.value, ctx);
			ctx.addVariable({	name: json.name, value: "0" });
			return node;
		},
		UziLogicalOrNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "logical_operation");
			appendField(node, "operator", "or");
			appendValue(node, "left", json.left, ctx);
			appendValue(node, "right", json.right, ctx);
			return node;
		},
		UziLogicalAndNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "logical_operation");
			appendField(node, "operator", "and");
			appendValue(node, "left", json.left, ctx);
			appendValue(node, "right", json.right, ctx);
			return node;
		},
		UziReturnNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			if (json.value) {
				node.setAttribute("type", "return_value");
				appendValue(node, "value", json.value, ctx);
			} else {
				node.setAttribute("type", "return");
			}
			return node;
		}
	};

	function getLiteralValue(node) {
		if (!node) return "0";
		if (node.__class__ == "UziPinLiteralNode") {
			return node.type + node.number;
		} else if (node.__class__ == "UziNumberLiteralNode"){
			return node.value.toString();
		} else {
			return "0";
		}
	}

	function initExternalVariable(node, json, ctx) {
		let parts = json.name.split(".");
		if (parts.length > 2) {
			debugger; // TODO(Richo): WTF?
		}
		let alias = parts[0];
		let name = parts[1];

		if (ctx.joysticks.some(j => j.name == alias)) {
			initJoystickCall(node, alias, name, json, ctx);
		} else {
			// NOTE(Richo): Fallback code...
			initRegularvariable(node, json, ctx);
		}
	}

	function initRegularVariable(node, json, ctx) {
		node.setAttribute("type", "variable");
		appendField(node, "variableName", json.name);
	}

	function initProcedureCall(node, json, ctx) {
		let types = ["proc_call_0args", "proc_call_1args",
								"proc_call_2args", "proc_call_3args"];
		if (json.arguments.length > 3) {
			throw "Max number of arguments for call blocks is 3";
		}
		node.setAttribute("type", types[json.arguments.length]);
		appendField(node, "scriptName", json.selector);
		let script = ctx.scriptNamed(json.selector);
		json.arguments.forEach(function (arg, index) {
			appendValue(node, "arg" + index, arg.value, ctx);
		});
	}

	function initFunctionCall(node, json, ctx) {
		let types = ["func_call_0args", "func_call_1args",
								"func_call_2args", "func_call_3args"];
		if (json.arguments.length > 3) {
			throw "Max number of arguments for call blocks is 3";
		}
		node.setAttribute("type", types[json.arguments.length]);
		appendField(node, "scriptName", json.selector);
		let script = ctx.scriptNamed(json.selector);
		json.arguments.forEach(function (arg, index) {
			appendValue(node, "arg" + index, arg.value, ctx);
		});
	}

	function initExternalCall(node, json, ctx) {
		let parts = json.selector.split(".");
		if (parts.length > 2) {
			debugger; // TODO(Richo): WTF?
		}
		let alias = parts[0];
		let selector = parts[1];

		if (ctx.motors.some(m => m.name == alias)) {
			initMotorCall(node, alias, selector, json, ctx);
		} else if (ctx.sonars.some(s => s.name == alias)) {
			initSonarCall(node, alias, selector, json, ctx);
		} else if (ctx.joysticks.some(j => j.name == alias)) {
			initJoystickCall(node, alias, selector, json, ctx);
		} else if (ctx.isButtonCall(alias, selector)) {
			initButtonCall(node, alias, selector, json, ctx);
		} else if (ctx.lists.some(l => l.name == alias)) {
			initListCall(node, alias, selector, json, ctx);
		} else {
			// NOTE(Richo): Fallback code...
			initPrimitiveCall(node, json, ctx);
		}
	}

	function initMotorCall(node, alias, selector, json, ctx) {
		let defaultArg = {__class__: "UziNumberLiteralNode", value: 0};
		let args = json.arguments.map(function (each) { return each.value; });
		if (selector == "forward" || selector == "backward") {
			node.setAttribute("type", "move_dcmotor");
			appendField(node, "motorName", alias);
			appendField(node, "direction", selector == "forward" ? "fwd" : "bwd");
			appendValue(node, "speed", args[0] || defaultArg, ctx);
		} else if (selector == "setSpeed") {
			node.setAttribute("type", "change_speed_dcmotor");
			appendField(node, "motorName", alias);
			appendValue(node, "speed", args[0] || defaultArg, ctx);
		} else if (selector == "getSpeed") {
			node.setAttribute("type", "get_speed_dcmotor");
			appendField(node, "motorName", alias);
		} else if (selector == "brake") {
			node.setAttribute("type", "stop_dcmotor");
			appendField(node, "motorName", alias);
		} else {
			// NOTE(Richo): Fallback code...
			initPrimitiveCall(node, json, ctx);
		}
	}

	function initSonarCall(node, alias, selector, json, ctx) {
		if (selector == "distance_mm") {
			node.setAttribute("type", "get_sonar_distance");
			appendField(node, "sonarName", alias);
			appendField(node, "unit", "mm");
		} else if (selector == "distance_cm") {
			node.setAttribute("type", "get_sonar_distance");
			appendField(node, "sonarName", alias);
			appendField(node, "unit", "cm");
		} else if (selector == "distance_m") {
			node.setAttribute("type", "get_sonar_distance");
			appendField(node, "sonarName", alias);
			appendField(node, "unit", "m");
		} else {
			// NOTE(Richo): Fallback code...
			initPrimitiveCall(node, json, ctx);
		}
	}

	function initJoystickCall(node, alias, selector, json, ctx) {
		if (selector == "x") {
			node.setAttribute("type", "get_joystick_x");
			appendField(node, "joystickName", alias);
		} else if (selector == "y") {
			node.setAttribute("type", "get_joystick_y");
			appendField(node, "joystickName", alias);
		} else	if (selector == "getAngle") {
			node.setAttribute("type", "get_joystick_angle");
			appendField(node, "joystickName", alias);
		} else if (selector == "getMagnitude") {
			node.setAttribute("type", "get_joystick_magnitude");
			appendField(node, "joystickName", alias);
		} else {
			// NOTE(Richo): Fallback code...
			initPrimitiveCall(node, json, ctx);
		}
	}

	function initButtonCall(node, alias, selector, json, ctx) {
		let defaultArg = {__class__: "UziNumberLiteralNode", value: 0};
		let args = json.arguments.map(function (each) { return each.value; });
		if (selector == "isPressed" || selector == "isReleased") {
			node.setAttribute("type", "button_check_state");
			appendValue(node, "pinNumber", args[0] || defaultArg, ctx);
			appendField(node, "state", selector == "isPressed" ? "press" : "release");
		} else if (selector == "waitForPress" || selector == "waitForRelease") {
			node.setAttribute("type", "button_wait_for_action");
			appendValue(node, "pinNumber", args[0] || defaultArg, ctx);
			appendField(node, "action", selector == "waitForPress" ? "press" : "release");
		} else	if (selector == "millisecondsHolding") {
			node.setAttribute("type", "button_ms_holding");
			appendValue(node, "pinNumber", args[0] || defaultArg, ctx);
		} else if (selector == "waitForHold" || selector == "waitForHoldAndRelease") {
			node.setAttribute("type", "button_wait_for_long_action");
			appendValue(node, "pinNumber", args[0] || defaultArg, ctx);
			appendField(node, "action", selector == "waitForHold" ? "press" : "release");
			appendField(node, "unit", "ms");
			appendValue(node, "time", args[1] || defaultArg, ctx);
		} else {
			// NOTE(Richo): Fallback code...
			initPrimitiveCall(node, json, ctx);
		}
	}

	function initListCall(node, alias, selector, json, ctx) {
		let defaultArg = {__class__: "UziNumberLiteralNode", value: 0};
		let args = json.arguments.map(function (each) { return each.value; });
		if (selector == "set") {
			node.setAttribute("type", "list_set");
			appendField(node, "listName", alias);
			appendValue(node, "index", args[0] || defaultArg, ctx);
			appendValue(node, "value", args[1] || defaultArg, ctx);
		} else if (selector == "get") {
			node.setAttribute("type", "list_get");
			appendField(node, "listName", alias);
			appendValue(node, "index", args[0] || defaultArg, ctx);
		} else if (selector == "push") {
			node.setAttribute("type", "list_push");
			appendField(node, "listName", alias);
			appendValue(node, "value", args[0] || defaultArg, ctx);
		} else {
			let selectors = {
				"pop": "list_pop",
				"clear": "list_clear",
				"count": "list_count",
				"size": "list_size",
				"get_random": "list_random",
				"sum": "list_sum",
				"avg": "list_avg",
				"max": "list_max",
				"min": "list_min"
			};
			let block_type = selectors[selector];
			if (block_type) {
				node.setAttribute("type", block_type);
				appendField(node, "listName", alias);
			} else {
				// NOTE(Richo): Fallback code...
				initPrimitiveCall(node, json, ctx);
			}
		}
	}

	function initPrimitiveCall(node, json, ctx) {
		let selector = json.selector;
		let args = json.arguments.map(function (each) { return each.value; });
		if (selector === "toggle") {
			node.setAttribute("type", "toggle_pin");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "turnOn") {
			node.setAttribute("type", "turn_onoff_pin");
			appendField(node, "pinState", "on");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "turnOff") {
			node.setAttribute("type", "turn_onoff_pin");
			appendField(node, "pinState", "off");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "isOn") {
			node.setAttribute("type", "is_onoff_pin");
			appendField(node, "pinState", "on");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "isOff") {
			node.setAttribute("type", "is_onoff_pin");
			appendField(node, "pinState", "off");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "write") {
			node.setAttribute("type", "write_pin");
			appendValue(node, "pinNumber", args[0], ctx);
			appendValue(node, "pinValue", args[1], ctx);
		} else if (selector === "read") {
			node.setAttribute("type", "read_pin");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "setServoDegrees") {
			node.setAttribute("type", "set_servo_degrees");
			appendValue(node, "pinNumber", args[0], ctx);
			appendValue(node, "servoValue", args[1], ctx);
		} else if (selector === "delayMs") {
			node.setAttribute("type", "delay");
			appendField(node, "unit", "ms");
			appendValue(node, "time", args[0], ctx);
		} else if (selector === "delayS") {
			node.setAttribute("type", "delay");
			appendField(node, "unit", "s");
			appendValue(node, "time", args[0], ctx);
		} else if (selector === "delayM") {
			node.setAttribute("type", "delay");
			appendField(node, "unit", "m");
			appendValue(node, "time", args[0], ctx);
		} else if (selector === "millis") {
			node.setAttribute("type", "elapsed_time");
			appendField(node, "unit", "ms");
		} else if (selector === "seconds") {
			node.setAttribute("type", "elapsed_time");
			appendField(node, "unit", "s");
		} else if (selector === "minutes") {
			node.setAttribute("type", "elapsed_time");
			appendField(node, "unit", "m");
		} else if (selector === "sin") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "sin");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "cos") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "cos");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "tan") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "tan");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "asin") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "asin");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "acos") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "acos");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "atan") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "atan");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "round") {
			node.setAttribute("type", "number_round");
			appendField(node, "operator", "round");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "ceil") {
			node.setAttribute("type", "number_round");
			appendField(node, "operator", "ceil");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "floor") {
			node.setAttribute("type", "number_round");
			appendField(node, "operator", "floor");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "sqrt") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "sqrt");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "abs") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "abs");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "ln") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "ln");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "log10") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "log10");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "exp") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "exp");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "pow10") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "pow10");
			appendValue(node, "number", args[0], ctx);
		} else if (selector === "+") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "ADD");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "-") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "MINUS");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "*") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "MULTIPLY");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "/") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "DIVIDE");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "**") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "POWER");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "==") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "==");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "!=") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "!=");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "<=") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "<=");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "<") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "<");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === ">=") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", ">=");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === ">") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", ">");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "!") {
			node.setAttribute("type", "logical_not");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "isEven") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "even");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "isOdd") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "odd");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "isPrime") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "prime");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "isWhole") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "whole");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "isPositive") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "positive");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "isNegative") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "negative");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "isDivisibleBy") {
			node.setAttribute("type", "number_divisibility");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "%") {
			node.setAttribute("type", "number_modulo");
			appendValue(node, "dividend", args[0], ctx);
			appendValue(node, "divisor", args[1], ctx);
		} else if (selector === "constrain") {
			node.setAttribute("type", "number_constrain");
			appendValue(node, "value", args[0], ctx);
			appendValue(node, "low", args[1], ctx);
			appendValue(node, "high", args[2], ctx);
		} else if (selector === "randomInt") {
			node.setAttribute("type", "number_random_int");
			appendValue(node, "from", args[0], ctx);
			appendValue(node, "to", args[1], ctx);
		} else if (selector === "random") {
			node.setAttribute("type", "number_random_float");
		} else if (selector === "setPinMode") {
			let validModes = ["INPUT", "OUTPUT", "INPUT_PULLUP"];
			if (args[1].__class__ == "UziNumberLiteralNode" &&
					args[1].value >= 0 && args[1].value < validModes.length) {
				node.setAttribute("type", "set_pin_mode");
				appendValue(node, "pinNumber", args[0], ctx);
				appendField(node, "mode", validModes[args[1].value] || "INPUT");
			} else {
				initProcedureCall(node, json, ctx);
			}
		} else if (selector === "getServoDegrees") {
			node.setAttribute("type", "get_servo_degrees");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "servoWrite") {
			// servoWrite(D3, 0.5) -> setServoDegrees(D3, 0.5 * 180);
			node.setAttribute("type", "set_servo_degrees");
			appendValue(node, "pinNumber", args[0], ctx);

			let value = create("block");
			value.setAttribute("type", "math_arithmetic");
			appendField(value, "operator", "MULTIPLY");
			appendValue(value, "left", args[1], ctx);
			let multiplier = create("block");
			multiplier.setAttribute("type", "number");
			appendField(multiplier, "value", 180);
			appendValueNode(value, "right", multiplier);

			appendValueNode(node, "servoValue", value);
		} else if (selector === "&") {
			node.setAttribute("type", "logical_operation");
			appendField(node, "operator", "and");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "|") {
			node.setAttribute("type", "logical_operation");
			appendField(node, "operator", "or");
			appendValue(node, "left", args[0], ctx);
			appendValue(node, "right", args[1], ctx);
		} else if (selector === "startTone") {
			node.setAttribute("type", "start_tone");
			appendValue(node, "pinNumber", args[0], ctx);
			appendValue(node, "tone", args[1], ctx);
		} else if (selector === "stopTone") {
			node.setAttribute("type", "stop_tone");
			appendValue(node, "pinNumber", args[0], ctx);
		} else if (selector === "playTone") {
			node.setAttribute("type", "play_tone");
			appendField(node, "unit", "ms");
			appendValue(node, "pinNumber", args[0], ctx);
			appendValue(node, "tone", args[1], ctx);
			appendValue(node, "time", args[2], ctx);
		} else if (selector === "stopToneAndWait") {
			node.setAttribute("type", "stop_tone_wait");
			appendField(node, "unit", "ms");
			appendValue(node, "pinNumber", args[0], ctx);
			appendValue(node, "time", args[1], ctx);
		} else if (selector === "isBetween") {
			node.setAttribute("type", "number_between");
			appendValue(node, "value", args[0], ctx);
			appendValue(node, "low", args[1], ctx);
			appendValue(node, "high", args[2], ctx);
		} else if (selector === "pin") {
			node.setAttribute("type", "pin_cast");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "number") {
			node.setAttribute("type", "number_cast");
			appendValue(node, "value", args[0], ctx);
		} else if (selector === "bool") {
			node.setAttribute("type", "boolean_cast");
			appendValue(node, "value", args[0], ctx);
		} else {
			/*
			NOTE(Richo): Fallback code. If we don't have a specific block for the selector
			we just generate a regular proc/func call. To know which one to generate we look
			at the ctx.path for a block node immediately before the current node.
			*/
			if (ctx.isInStatementPosition(json)) {
				initProcedureCall(node, json, ctx);
			} else {
				initFunctionCall(node, json, ctx);
			}
		}
	}

	function createShadow(type) {
		let node = create("shadow");
		node.setAttribute("type", type);
		if (type == UziBlock.types.PIN) {
			appendField(node, "pinNumber", "D13");
		} else if (type == UziBlock.types.NUMBER) {
			appendField(node, "value", "1");
		} else if (type == UziBlock.types.BOOLEAN) {
			appendField(node, "value", "true");
		} else {
			return null; // Invalid type
		}
		return node;
	}

	function createCast(type) {
		let node = create("block");
		node.setAttribute("type", type + "_cast");
		return node;
	}

	function createShadowFor(blockType, valueName) {
		let input = Object.values(UziBlock.spec[blockType].inputs)
											.find(each => each.name == valueName);
		if (!input) return null;
		if (!input.types) return null;

		let preferredType = input.types[0];
		return createShadow(preferredType);
	}

	function createCastFor(blockType, valueName, value) {
		let input = Object.values(UziBlock.spec[blockType].inputs)
											.find(each => each.name == valueName);
		if (!input) return value;
		if (!input.types) return value;

		let valueType = UziBlock.spec[value.getAttribute("type")].type;
		if (!valueType) return value;

		let types = new Set(input.types);
		if (types.has(valueType)) return value;

		let preferredType = input.types[0];
		let cast = createCast(preferredType);
		appendValueNode(cast, "value", value);
		return cast;
	}

	function appendValue(node, name, json, ctx) {
		appendValueNode(node, name, generateXMLForExpression(json, ctx));
	}

	function appendValueNode(node, name, value) {
		let child = create("value");
		child.setAttribute("name", name);

		value = createCastFor(node.getAttribute("type"), name, value);

		let shadow = createShadowFor(node.getAttribute("type"), name);
		if (shadow) {
			child.appendChild(shadow);
			if (shadow.getAttribute("type") == value.getAttribute("type")) {
				shadow.childNodes.forEach(child => shadow.removeChild(child));
				value.childNodes.forEach(child => shadow.appendChild(child.cloneNode(true)));
			} else {
				child.appendChild(value);
			}
		} else {
			child.appendChild(value);
		}
		node.appendChild(child);
	}

	function createImportBlock(json, ctx) {
		let node = create("block");
		node.setAttribute("type", "import");
		appendField(node, "alias", json.alias);
		appendField(node, "path", json.path);

		// TODO(Richo): Store initialization block as comment or generate blocks?
		let code = JSONX.stringify(json.initializationBlock, null, 2);
		let comment = create("comment");
		comment.setAttribute("pinned", "false");
		comment.setAttribute("h", "160");
		comment.setAttribute("w", "320");
		comment.textContent = code;
		node.appendChild(comment);

		return node;
	}

	function createHereBeDragonsBlock(type, stmt, ctx) {
		let node = create("block");
		node.setAttribute("type", type);

		/*
		TODO(Richo): Get actual code to show in the comment and maybe make it visible
		in a read-only field. The ast should probably be preserved anyway.
		*/
		let ast = JSONX.stringify(stmt, null, 2);
		let comment = create("comment");
		comment.setAttribute("pinned", "false");
		comment.setAttribute("h", "160");
		comment.setAttribute("w", "320");
		comment.textContent = ast;

		node.appendChild(comment);
		return node;
	}

	function appendStatements(node, name, body, ctx) {
		let statements = body.statements.map(stmt => {
			try {
				ctx.path.push(body);
				return generateXMLForStatement(stmt, ctx);
			} finally {
				ctx.path.pop();
			}
		});

		let len = statements.length;
		if (len <= 0) return;
		let child = create("statement");
		child.setAttribute("name", name);
		child.appendChild(statements[0]);
		node.appendChild(child);
		if (len > 1) {
			let cur = statements[0];
			for (let i = 1; i < len; i++) {
				let next = create("next");
				next.appendChild(statements[i]);
				cur.appendChild(next);
				cur = statements[i];
			}
		}
	}

	function appendField(node, name, content) {
		let field = create("field");
		field.setAttribute("name", name);
		field.textContent = content;
		node.appendChild(field);
	}

	function create(name) {
		return document.createElement(name);
	}

	function generateXMLForStatement(stmt, ctx) {
		try {
			return generateXMLFor(stmt, ctx);
		} catch (err) {
			return createHereBeDragonsBlock("here_be_dragons_stmt", stmt, ctx);
		}
	}

	function generateXMLForExpression(expr, ctx) {
		try {
			return generateXMLFor(expr, ctx);
		} catch (err) {
			return createHereBeDragonsBlock("here_be_dragons_expr", expr, ctx);
		}
	}

	function generateXMLForScript(script, ctx) {
		try {
			return generateXMLFor(script, ctx);
		} catch (err) {
			return createHereBeDragonsBlock("here_be_dragons_script", script, ctx);
		}
	}

	function generateXMLFor(json, ctx) {
		let type = json.__class__;
		let func = dispatchTable[type];
		if (func == undefined) {
			console.log(json);
			throw "CODEGEN ERROR: Type not found '" + type + "'";
		}
		try {
			ctx.path.push(json);
			return func(json, ctx);
		}	finally {
			ctx.path.pop();
		}
	}

	return {
		generate: function (json) {
			let ctx = {
				path: [],
				variables: [],
				motors: [],
				sonars: [],
				joysticks: [],
				lists: [],

				addVariable: function (variable) {
					if (ctx.variables.some(v => v.name == variable.name)) return;
					variable.index = ctx.variables.length;
					ctx.variables.push(variable);
				},
				addMotor: function (motor) {
					motor.index = ctx.motors.length;
					ctx.motors.push(motor);
				},
				addSonar: function (sonar) {
					sonar.index = ctx.sonars.length;
					ctx.sonars.push(sonar);
				},
				addJoystick: function (joystick) {
					joystick.index = ctx.joysticks.length;
					ctx.joysticks.push(joystick);
				},
				addList: function (list) {
					list.index = ctx.lists.length;
					ctx.lists.push(list);
				},

				scriptNamed: function (name) {
					return ctx.path[0].scripts.find(function (each) {
						return each.name === name;
					});
				},
				isTask: function (name) {
					let script = ctx.scriptNamed(name);
					return script !== undefined && script.__class__ === "UziTaskNode";
				},
				isProcedure: function (name) {
					let script = ctx.scriptNamed(name);
					return script !== undefined && script.__class__ === "UziProcedureNode";
				},
				isFunction: function (name) {
					let script = ctx.scriptNamed(name);
					return script !== undefined && script.__class__ === "UziFunctionNode";
				},
				isInStatementPosition: function (json) {
					// NOTE(Richo): Returns true if the previous node in the path is a block
					let index = ctx.path.indexOf(json);
					if (index < 1 || index >= ctx.path.length) return false;
					return ctx.path[index - 1].__class__ == "UziBlockNode";
				},
				isButtonCall: function (alias, selector) {
					return ctx.path[0].imports.some(imp => imp.path == "Buttons.uzi" && imp.alias == alias);
				}
			};

			// TODO(Richo): Preserve old metadata somehow?
			return {
        version: UziBlock.version,
        blocks: Blockly.Xml.domToText(generateXMLFor(json, ctx)),
        motors: ctx.motors,
        sonars: ctx.sonars,
        joysticks: ctx.joysticks,
        variables: ctx.variables,
				lists: ctx.lists,
      };
		}
	}
})();
