let ASTToBlocks = (function () {

	let dispatchTable = {
		UziProgramNode: function (json, ctx) {
			let node = create("xml");
			json.scripts.forEach(function (script) {
				node.appendChild(generateXMLFor(script, ctx));
			});
			return node;
		},
		UziTaskNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", json.state === "once" ? "task" : "timer");
			appendField(node, "taskName", json.name);
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
				debugger;
			}
			node.setAttribute("type", types[json.arguments.length]);
			let script = ctx.scriptNamed(json.name);
			script.arguments.forEach(function (arg, i) {
				appendField(node, "arg" + i, arg.name);
			});
			appendField(node, "procName", json.name);
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziFunctionNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			let types = ["func_definition_0args", "func_definition_1args",
									"func_definition_2args", "func_definition_3args"];
			if (json.arguments.length > 3) {
				debugger;
			}
			node.setAttribute("type", types[json.arguments.length]);
			let script = ctx.scriptNamed(json.name);
			script.arguments.forEach(function (arg, i) {
				appendField(node, "arg" + i, arg.name);
			});
			appendField(node, "funcName", json.name);
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziCallNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			if (ctx.isTask(json.selector)) {
				node.setAttribute("type", "run_task");
				appendField(node, "taskName", json.selector);
			} else if (ctx.isProcedure(json.selector)) {
				let types = ["proc_call_0args", "proc_call_1args",
										"proc_call_2args", "proc_call_3args"];
				if (json.arguments.length > 3) { debugger; }
				node.setAttribute("type", types[json.arguments.length]);
				appendField(node, "procName", json.selector);
				let script = ctx.scriptNamed(json.selector);
				json.arguments.forEach(function (arg, index) {
					appendValue(node, "arg" + index, generateXMLFor(arg.value, ctx));
				});
			} else if (ctx.isFunction(json.selector)) {
				let types = ["func_call_0args", "func_call_1args",
										"func_call_2args", "func_call_3args"];
				if (json.arguments.length > 3) { debugger; }
				node.setAttribute("type", types[json.arguments.length]);
				appendField(node, "funcName", json.selector);
				let script = ctx.scriptNamed(json.selector);
				json.arguments.forEach(function (arg, index) {
					appendValue(node, "arg" + index, generateXMLFor(arg.value, ctx));
				});
			} else {
				initPrimitive(node, json, ctx);
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
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziScriptStopNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "stop_task");
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziScriptResumeNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "resume_task");
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziScriptPauseNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "pause_task");
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziConditionalNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type",
				json.falseBranch.statements.length > 0 ?
				"conditional_full" : "conditional_simple");
			appendValue(node, "condition", generateXMLFor(json.condition, ctx));
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
			appendValue(node, "times", generateXMLFor(json.times, ctx));
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziWhileNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "repeat");
			appendField(node, "negate", json.negated);
			appendValue(node, "condition", generateXMLFor(json.condition, ctx));
			appendStatements(node, "statements", json.post, ctx);
			return node;
		},
		UziUntilNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "repeat");
			appendField(node, "negate", json.negated);
			appendValue(node, "condition", generateXMLFor(json.condition, ctx));
			appendStatements(node, "statements", json.post, ctx);
			return node;
		},
		UziForNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "for");
			appendField(node, "variable", json.counter.name);
			appendValue(node, "start", generateXMLFor(json.start, ctx));
			appendValue(node, "stop", generateXMLFor(json.stop, ctx));
			appendValue(node, "step", generateXMLFor(json.step, ctx));
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
				appendValue(node, "value", generateXMLFor(json.right.arguments[1].value, ctx));
			} else {
				node.setAttribute("type", "set_variable");
				appendField(node, "variableName", json.left.name);
				appendValue(node, "value", generateXMLFor(json.right, ctx));
			}
			return node;
		},
		UziVariableNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "variable");
			appendField(node, "variableName", json.name);
			return node;
		},
		UziLogicalOrNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "logic_operation");
			appendField(node, "operator", "or");
			appendValue(node, "left", generateXMLFor(json.left, ctx));
			appendValue(node, "right", generateXMLFor(json.right, ctx));
			return node;
		},
		UziLogicalAndNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "logic_operation");
			appendField(node, "operator", "and");
			appendValue(node, "left", generateXMLFor(json.left, ctx));
			appendValue(node, "right", generateXMLFor(json.right, ctx));
			return node;
		},
		UziReturnNode: function (json, ctx) {
			let node = create("block");
			node.setAttribute("id", json.id);
			if (json.value) {
				node.setAttribute("type", "return_value");
				appendValue(node, "value", generateXMLFor(json.value, ctx));
			} else {
				node.setAttribute("type", "return");
			}
			return node;
		}
	};

	function initPrimitive(node, json, ctx) {
		let selector = json.selector;
		let args = json.arguments.map(function (each) { return each.value; });
		if (selector === "toggle") {
			node.setAttribute("type", "toggle_pin");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "turnOn") {
			node.setAttribute("type", "turn_onoff_pin");
			appendField(node, "pinState", "on");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "turnOff") {
			node.setAttribute("type", "turn_onoff_pin");
			appendField(node, "pinState", "off");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "isOn") {
			node.setAttribute("type", "is_onoff_pin");
			appendField(node, "pinState", "on");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "isOff") {
			node.setAttribute("type", "is_onoff_pin");
			appendField(node, "pinState", "off");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "write") {
			node.setAttribute("type", "write_pin");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
			appendValue(node, "pinValue", generateXMLFor(args[1], ctx));
		} else if (selector === "read") {
			node.setAttribute("type", "read_pin");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "setServoDegrees") {
			node.setAttribute("type", "set_servo_degrees");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
			appendValue(node, "servoValue", generateXMLFor(args[1], ctx));
		} else if (selector === "delayMs") {
			node.setAttribute("type", "delay");
			appendField(node, "unit", "ms");
			appendValue(node, "time", generateXMLFor(args[0], ctx));
		} else if (selector === "delayS") {
			node.setAttribute("type", "delay");
			appendField(node, "unit", "s");
			appendValue(node, "time", generateXMLFor(args[0], ctx));
		} else if (selector === "delayM") {
			node.setAttribute("type", "delay");
			appendField(node, "unit", "m");
			appendValue(node, "time", generateXMLFor(args[0], ctx));
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
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "cos") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "cos");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "tan") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "tan");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "asin") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "asin");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "acos") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "acos");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "atan") {
			node.setAttribute("type", "number_trig");
			appendField(node, "operator", "atan");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "round") {
			node.setAttribute("type", "number_round");
			appendField(node, "operator", "round");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "ceil") {
			node.setAttribute("type", "number_round");
			appendField(node, "operator", "ceil");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "floor") {
			node.setAttribute("type", "number_round");
			appendField(node, "operator", "floor");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "sqrt") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "sqrt");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "abs") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "abs");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "ln") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "ln");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "log10") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "log10");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "exp") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "exp");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "pow10") {
			node.setAttribute("type", "number_operation");
			appendField(node, "operator", "pow10");
			appendValue(node, "number", generateXMLFor(args[0], ctx));
		} else if (selector === "+") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "ADD");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "-") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "MINUS");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "*") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "MULTIPLY");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "/") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "DIVIDE");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "**") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "operator", "POWER");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "==") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "==");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "!=") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "!=");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "<=") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "<=");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "<") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", "<");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === ">=") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", ">=");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === ">") {
			node.setAttribute("type", "logical_compare");
			appendField(node, "operator", ">");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "!") {
			node.setAttribute("type", "logical_not");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
		} else if (selector === "isEven") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "even");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
		} else if (selector === "isOdd") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "odd");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
		} else if (selector === "isPrime") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "prime");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
		} else if (selector === "isWhole") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "whole");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
		} else if (selector === "isPositive") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "positive");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
		} else if (selector === "isNegative") {
			node.setAttribute("type", "number_property");
			appendField(node, "property", "negative");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
		} else if (selector === "isDivisibleBy") {
			node.setAttribute("type", "number_divisibility");
			appendValue(node, "left", generateXMLFor(args[0], ctx));
			appendValue(node, "right", generateXMLFor(args[1], ctx));
		} else if (selector === "%") {
			node.setAttribute("type", "number_modulo");
			appendValue(node, "dividend", generateXMLFor(args[0], ctx));
			appendValue(node, "divisor", generateXMLFor(args[1], ctx));
		} else if (selector === "constrain") {
			node.setAttribute("type", "number_constrain");
			appendValue(node, "value", generateXMLFor(args[0], ctx));
			appendValue(node, "low", generateXMLFor(args[1], ctx));
			appendValue(node, "high", generateXMLFor(args[2], ctx));
		} else if (selector === "randomInt") {
			node.setAttribute("type", "number_random_int");
			appendValue(node, "from", generateXMLFor(args[0], ctx));
			appendValue(node, "to", generateXMLFor(args[1], ctx));
		} else if (selector === "random") {
			node.setAttribute("type", "number_random_float");
		} else if (selector === "setPinMode") {
			node.setAttribute("type", "set_pin_mode");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
			let validModes = ["INPUT", "OUTPUT", "INPUT_PULLUP"];
			if (args[1].__class__ == "UziNumberLiteralNode") {
				appendField(node, "mode", validModes[args[1].value] || "INPUT");
			} else {
				debugger;
			}
		} else {
			console.log(json);
			debugger;
			throw "Selector not found: " + selector;
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

	function createShadowFor(blockType, valueName) {
		let input = Object.values(UziBlock.spec[blockType].inputs)
											.find(each => each.name == valueName);
		if (!input) return null;
		if (!input.types) return null;

		let preferredType = input.types[0];
		return createShadow(preferredType);
	}

	function appendValue(node, name, value) {
		let child = create("value");
		child.setAttribute("name", name);
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

	function appendStatements(node, name, body, ctx) {
		let statements = body.statements.map(function(stmt) {
			return generateXMLFor(stmt, ctx);
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

	function generateXMLFor(json, ctx) {
		let type = json.__class__;
		let func = dispatchTable[type];
		if (func == undefined) {
			console.log(json);
			debugger;
			throw "CODEGEN ERROR: Type not found '" + type + "'";
		}
		try {
			ctx.path.push(json);
			return func(json, ctx);
		}
		finally {
			ctx.path.pop();
		}
	}

	return {
		generate: function (json) {
			let ctx = {
				path: [],
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
				}
			};
			return generateXMLFor(json, ctx);
		}
	}
})();
