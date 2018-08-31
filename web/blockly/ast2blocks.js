var ASTToBlocks = (function () {
	
	var dispatchTable = {
		UziProgramNode: function (json, ctx) {
			var node = create("xml");
			json.scripts.forEach(function (script) {
				node.appendChild(generateXMLFor(script, ctx));
			});
			return node;
		},
		UziTaskNode: function (json, ctx) {
			var node = create("block");
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
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "procedures_defnoreturn");
			var mut = create("mutation");
			var script = ctx.scriptNamed(json.name);
			script.arguments.forEach(function (arg) {
				var argNode = create("arg");
				argNode.setAttribute("name", arg.name);
				mut.appendChild(argNode);
			});
			node.appendChild(mut);			
			appendField(node, "NAME", json.name);
			appendStatements(node, "STACK", json.body, ctx);
			return node;
		},
		UziFunctionNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "procedures_defreturn");
			var mut = create("mutation");
			var script = ctx.scriptNamed(json.name);
			script.arguments.forEach(function (arg) {
				var argNode = create("arg");
				argNode.setAttribute("name", arg.name);
				mut.appendChild(argNode);
			});
			node.appendChild(mut);			
			appendField(node, "NAME", json.name);
			appendStatements(node, "STACK", json.body, ctx);
			return node;
		},
		UziCallNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			if (json.primitiveName !== null) {
				initPrimitive(node, json, ctx);
			} else if (ctx.isTask(json.selector)) {
				node.setAttribute("type", "run_task");
				appendField(node, "taskName", json.selector);
			} else if (ctx.isProcedure(json.selector)) {
				node.setAttribute("type", "procedures_callnoreturn");
				var mut = create("mutation");
				mut.setAttribute("name", json.selector);
				var script = ctx.scriptNamed(json.selector);
				json.arguments.forEach(function (arg, index) {
					var argNode = create("arg");
					argNode.setAttribute("name", script.arguments[index].name);
					mut.appendChild(argNode);
					appendValue(node, "ARG" + index, generateXMLFor(arg.value, ctx));
				});
				node.appendChild(mut);
			} else if (ctx.isFunction(json.selector)) {
				node.setAttribute("type", "procedures_callreturn");
				var mut = create("mutation");
				mut.setAttribute("name", json.selector);
				var script = ctx.scriptNamed(json.selector);
				json.arguments.forEach(function (arg, index) {
					var argNode = create("arg");
					argNode.setAttribute("name", script.arguments[index].name);
					mut.appendChild(argNode);
					appendValue(node, "ARG" + index, generateXMLFor(arg.value, ctx));
				});
				node.appendChild(mut);
			} else {
				throw "Invalid call: "+ json.selector;
			}
			return node;
		},
		UziNumberLiteralNode: function (json, ctx) {
			var node = create("shadow");
			node.setAttribute("id", json.id);
			if (json.value == Math.PI) {			
				node.setAttribute("type", "math_constant");
				appendField(node, "CONSTANT", "PI");
			} else if (json.value == Math.E) {
				node.setAttribute("type", "math_constant");
				appendField(node, "CONSTANT", "E");
			} else if (json.value == Math.SQRT2) {
				node.setAttribute("type", "math_constant");
				appendField(node, "CONSTANT", "SQRT2");
			} else if (json.value == Math.SQRT1_2) {
				node.setAttribute("type", "math_constant");
				appendField(node, "CONSTANT", "SQRT1_2");
			} else if (json.value == Infinity) {
				node.setAttribute("type", "math_constant");
				appendField(node, "CONSTANT", "INFINITY");
			} else if (json.value == 1.61803398875) {
				node.setAttribute("type", "math_constant");
				appendField(node, "CONSTANT", "GOLDEN_RATIO");
			} else {
				node.setAttribute("type", "math_number");
				appendField(node, "NUM", json.value);	
			}
			return node;
		},
		UziPinLiteralNode: function (json, ctx) {
			var node = create("shadow");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "pin");
			appendField(node, "pinNumber", json.type + json.number);
			return node;
		},
		UziScriptStartNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "start_task");
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziScriptStopNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "stop_task");
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziScriptResumeNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "resume_task");
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziScriptPauseNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "pause_task");
			appendField(node, "taskName", json.scripts[0]);
			return node;
		},
		UziConditionalNode: function (json, ctx) {
			var node = create("block");
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
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "forever");
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziRepeatNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "repeat_times");
			appendValue(node, "times", generateXMLFor(json.times, ctx));
			appendStatements(node, "statements", json.body, ctx);
			return node;
		},
		UziWhileNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "repeat");
			appendField(node, "negate", json.negated);
			appendValue(node, "condition", generateXMLFor(json.condition, ctx));
			appendStatements(node, "statements", json.post, ctx);
			return node;
		},
		UziForNode: function (json, ctx) {
			var node = create("block");
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
			var node = create("block");
			node.setAttribute("id", json.id);
			// HACK(Richo): Check if the assignment represents a variable increment
			if (json.right.__class__ === "UziCallNode" 
				&& json.right.selector === "+"
				&& json.right.arguments[0].value.__class__ === "UziVariableNode"
				&& json.right.arguments[0].value.name == json.left.name) {
				node.setAttribute("type", "math_change");
				appendField(node, "VAR", json.left.name);
				appendValue(node, "DELTA", generateXMLFor(json.right.arguments[1].value, ctx));
			} else {
				node.setAttribute("type", "variables_set");
				appendField(node, "VAR", json.left.name);
				appendValue(node, "VALUE", generateXMLFor(json.right, ctx));
			}
			return node;
		},
		UziVariableNode: function (json, ctx) {
			var node = create("block");
			node.setAttribute("id", json.id);
			node.setAttribute("type", "variables_get");
			appendField(node, "VAR", json.name);
			return node;
		},
	};
	
	function initPrimitive(node, json, ctx) {
		var selector = json.selector;
		var args = json.arguments.map(function (each) { return each.value; });
		if (selector === "toggle") {
			node.setAttribute("type", "toggle_variable");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "turnOn") {
			node.setAttribute("type", "turn_pin_variable");
			appendField(node, "pinState", "on");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "turnOff") {
			node.setAttribute("type", "turn_pin_variable");
			appendField(node, "pinState", "off");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "isOn") {
			node.setAttribute("type", "is_pin_variable");
			appendField(node, "pinState", "on");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "write") {
			node.setAttribute("type", "write_pin_variable");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
			appendValue(node, "pinValue", generateXMLFor(args[1], ctx));
		} else if (selector === "read") {
			node.setAttribute("type", "read_pin_variable");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
		} else if (selector === "servoDegrees") {
			node.setAttribute("type", "degrees_servo_variable");
			appendValue(node, "pinNumber", generateXMLFor(args[0], ctx));
			appendValue(node, "servoValue", generateXMLFor(args[1], ctx));
		} else if (selector === "delayMs") {
			node.setAttribute("type", "delay");
			appendField(node, "unit", "ms");
			appendValue(node, "time", generateXMLFor(args[0], ctx));
		} else if (selector === "seconds") {
			node.setAttribute("type", "elapsed_time");
			appendField(node, "unit", "s");
		} else if (selector === "sin") {
			node.setAttribute("type", "math_trig");
			appendField(node, "OP", "SIN");
			appendValue(node, "NUM", generateXMLFor(args[0], ctx));
		} else if (selector === "round") {
			node.setAttribute("type", "math_round");
			appendField(node, "OP", "ROUND");
			appendValue(node, "NUM", generateXMLFor(args[0], ctx));
		} else if (selector === "sqrt") {
			node.setAttribute("type", "math_single");
			appendField(node, "OP", "ROOT");
			appendValue(node, "NUM", generateXMLFor(args[0], ctx));
		} else if (selector === "+") {
			node.setAttribute("type", "math_arithmetic");
			appendField(node, "OP", "ADD");
			appendValue(node, "A", generateXMLFor(args[0], ctx));
			appendValue(node, "B", generateXMLFor(args[1], ctx));
		} else if (selector === "&&") {
			node.setAttribute("type", "logic_operation");
			appendField(node, "OP", "AND");
			appendValue(node, "A", generateXMLFor(args[0], ctx));
			appendValue(node, "B", generateXMLFor(args[1], ctx));
		} else if (selector === "==") {
			node.setAttribute("type", "logic_compare");
			appendField(node, "OP", "EQ");
			appendValue(node, "A", generateXMLFor(args[0], ctx));
			appendValue(node, "B", generateXMLFor(args[1], ctx));
		} else if (selector === "!") {
			node.setAttribute("type", "logic_negate");
			appendValue(node, "BOOL", generateXMLFor(args[0], ctx));
		} else if (selector === "isEven") {
			node.setAttribute("type", "math_number_property");
			var mut = create("mutation");
			mut.setAttribute("divisor_input", "false");
			node.appendChild(mut);
			appendField(node, "PROPERTY", "EVEN");
			appendValue(node, "NUMBER_TO_CHECK", generateXMLFor(args[0], ctx));
		} else if (selector === "%") {
			node.setAttribute("type", "math_modulo");
			appendValue(node, "DIVIDEND", generateXMLFor(args[0], ctx));
			appendValue(node, "DIVISOR", generateXMLFor(args[1], ctx));
		} else if (selector === "constrain") {
			node.setAttribute("type", "math_constrain");
			appendValue(node, "VALUE", generateXMLFor(args[0], ctx));
			appendValue(node, "LOW", generateXMLFor(args[1], ctx));
			appendValue(node, "HIGH", generateXMLFor(args[2], ctx));
		} else if (selector === "randomInt") {
			node.setAttribute("type", "math_random_int");
			appendValue(node, "FROM", generateXMLFor(args[0], ctx));
			appendValue(node, "TO", generateXMLFor(args[1], ctx));
		} else if (selector === "random") {
			node.setAttribute("type", "math_random_float");
		} else {
			console.log(json);
			debugger;
			throw "Selector not found: " + selector;
		}
	}
	
	function appendValue(node, name, value) {
		var child = create("value");
		child.setAttribute("name", name);
		child.appendChild(value);
		node.appendChild(child);
	}
	
	function appendStatements(node, name, body, ctx) {
		var statements = body.statements.map(function(stmt) {
			return generateXMLFor(stmt, ctx);
		});
		var len = statements.length;
		if (len <= 0) return;
		var child = create("statement");
		child.setAttribute("name", name);
		child.appendChild(statements[0]);
		node.appendChild(child);
		if (len > 1) {
			var cur = statements[0];
			for (var i = 1; i < len; i++) {
				var next = create("next");
				next.appendChild(statements[i]);
				cur.appendChild(next);
				cur = statements[i];
			}
		}
	}
	
	function appendField(node, name, content) {
		var field = create("field");
		field.setAttribute("name", name);
		field.textContent = content;
		node.appendChild(field);
	}
	
	function create(name) {
		return document.createElement(name);
	}
	
	function generateXMLFor(json, ctx) {
		var type = json.__class__;
		var func = dispatchTable[type];
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
			var ctx = {
				path: [json],
				scriptNamed: function (name) {
					return ctx.path[0].scripts.find(function (each) {
						return each.name === name;
					});
				},
				isTask: function (name) {
					var script = ctx.scriptNamed(name);
					return script !== undefined && script.__class__ === "UziTaskNode";
				},
				isProcedure: function (name) {
					var script = ctx.scriptNamed(name);
					return script !== undefined && script.__class__ === "UziProcedureNode";
				},
				isFunction: function (name) {
					var script = ctx.scriptNamed(name);
					return script !== undefined && script.__class__ === "UziFunctionNode";
				}
			};
			return generateXMLFor(json, ctx);
		}
	}
})();