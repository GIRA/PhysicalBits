var BlocksToAST = (function () {

	var builder = {
		program: function (id, imports, globals, scripts) {
			return {
				__class__: "UziProgramNode",
				imports: imports,
				globals: globals.map(function (varName) {
					return builder.variableDeclaration(id, varName);
				}),
				scripts: scripts,
				primitives: []
			};
		},
		import: function (id, alias, path, initBlock) {
			return {
				__class__: "UziImportNode",
				alias: alias,
				path: path,
				initializationBlock: initBlock
			};
		},
		task: function (id, name, argumentNames, state, tickingRate, statements) {
			return {
				__class__: "UziTaskNode",
				id: id,
				name: name,
				arguments: argumentNames.map(function (varName) {
					return builder.variableDeclaration(id, varName);
				}),
				state: state,
				tickingRate: tickingRate,
				body: builder.block(id, statements)
			};
		},
		procedure: function (id, name, argumentNames, statements) {
			return {
				__class__: "UziProcedureNode",
				id: id,
				name: name,
				arguments: argumentNames.map(function (varName) {
					return builder.variableDeclaration(id, varName);
				}),
				body: builder.block(id, statements)
			};
		},
		function: function (id, name, argumentNames, statements) {
			return {
				__class__: "UziFunctionNode",
				id: id,
				name: name,
				arguments: argumentNames.map(function (varName) {
					return builder.variableDeclaration(id, varName);
				}),
				body: builder.block(id, statements)
			};
		},
		scriptCall: function (id, selector, args) {
			return {
				__class__: "UziCallNode",
				id: id,
				selector: selector,
				arguments: args.map(function (arg) {
					return {
						__class__: "Association",
						key: arg.name,
						value: arg.value
					};
				})
			};
		},
		primitiveCall: function (id, selector, args) {
			return {
				__class__: "UziCallNode",
				id: id,
				selector: selector,
				arguments: args.map(function (value) {
					return {
						__class__: "Association",
						key: null,
						value: value
					};
				})
			};
		},
		block: function (id, statements) {
			return {
				__class__: "UziBlockNode",
				id: id,
				statements: statements
			};
		},
		tickingRate: function (id, runningTimes, tickingScale) {
			return {
				__class__: "UziTickingRateNode",
				id: id,
				value: runningTimes,
				scale: tickingScale
			};
		},
		forever: function (id, statements) {
			return {
				__class__: "UziForeverNode",
				id: id,
				body: builder.block(id, statements)
			};
		},
		variableDeclaration: function (id, variableName) {
			return {
				__class__: "UziVariableDeclarationNode",
				id: id,
				name: variableName,
				value: null
			};
		},
		for: function (id, counterName, start, stop, step, statements) {
			return {
				__class__: "UziForNode",
				id: id,
				counter: builder.variableDeclaration(id, counterName),
				start: start,
				stop: stop,
				step: step,
				body: builder.block(id, statements)
			};
		},
		number: function (id, value) {
			return {
				__class__: "UziNumberLiteralNode",
				id: id,
				value: value
			};
		},
		pin: function (id, type, number) {
			return {
				__class__: "UziPinLiteralNode",
				id: id,
				type: type,
				number: number
			};
		},
		variable: function (id, variableName) {
			return {
				__class__: "UziVariableNode",
				id: id,
				name: variableName
			};
		},
		start: function (id, scripts) {
			return {
				__class__: "UziScriptStartNode",
				id: id,
				scripts: scripts
			};
		},
		stop: function (id, scripts) {
			return {
				__class__: "UziScriptStopNode",
				id: id,
				scripts: scripts
			};
		},
		resume: function (id, scripts) {
			return {
				__class__: "UziScriptResumeNode",
				id: id,
				scripts: scripts
			};
		},
		pause: function (id, scripts) {
			return {
				__class__: "UziScriptPauseNode",
				id: id,
				scripts: scripts
			};
		},
		conditional: function (id, condition, trueBranch, falseBranch) {
			return {
				__class__: "UziConditionalNode",
				id: id,
				condition: condition,
				trueBranch: builder.block(id, trueBranch),
				falseBranch: builder.block(id, falseBranch)
			};
		},
		repeat: function (id, times, statements) {
			return {
				__class__: "UziRepeatNode",
				id: id,
				times: times,
				body: builder.block(id, statements)
			};
		},
		while: function (id, condition, statements, negated) {
			return {
				__class__: "UziWhileNode",
				id: id,
				pre: builder.block(id, []),
				condition: condition,
				post: builder.block(id, statements),
				negated: negated
			};
		},
		assignment: function (id, name, value) {
			return {
				__class__: "UziAssignmentNode",
				id: id,
				left: builder.variable(id, name),
				right: value
			};
		},
		return: function (id, value) {
			return {
				__class__: "UziReturnNode",
				id: id,
				value: value
			};
		},
		logicalAnd: function (id, left, right) {
			return {
				__class__: "UziLogicalAndNode",
				id: id,
				left: left,
				right: right
			};
		},
		logicalOr: function (id, left, right) {
			return {
				__class__: "UziLogicalOrNode",
				id: id,
				left: left,
				right: right
			};
		}
	};

	var topLevelBlocks = ["task", "timer", "procedures_defnoreturn", "procedures_defreturn"];
	var dispatchTable =  {
		task: function (block, ctx) {
			var id = XML.getId(block);
			var taskName = asIdentifier(XML.getChildNode(block, "taskName").innerText);
			var statements = generateCodeForStatements(block, ctx);
			return builder.task(id, taskName, [], "once", null, statements);
		},
		forever: function (block, ctx) {
			var id = XML.getId(block);
			var statements = generateCodeForStatements(block, ctx);
			return builder.forever(id, statements);
		},
		for: function (block, ctx) {
			var id = XML.getId(block);
			var variableName = asIdentifier(XML.getChildNode(block, "variable").innerText);
			var start = generateCodeForValue(block, ctx, "start");
			var stop = generateCodeForValue(block, ctx, "stop");
			var step = generateCodeForValue(block, ctx, "step");
			var statements = generateCodeForStatements(block, ctx);
			return builder.for(id, variableName, start, stop, step, statements);
		},
		number: function (block, ctx) {
			var id = XML.getId(block);
			var value = parseFloat(XML.getChildNode(block, "value").innerText);
			return builder.number(id, value);
		},
		turn_pin_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinState = XML.getChildNode(block, "pinState").innerText;
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var selector = pinState === "on" ? "turnOn" : "turnOff";
			return builder.primitiveCall(id, selector, [pinNumber]);
		},
		variable: function (block, ctx) {
			var id = XML.getId(block);
			var variableName = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			if (!ctx.isLocalDefined(variableName)) {
				ctx.addGlobal(variableName);
			}
			return builder.variable(id, variableName);
		},
		delay: function (block, ctx) {
			var id = XML.getId(block);
			var unit = XML.getChildNode(block, "unit").innerText;
			var time = generateCodeForValue(block, ctx, "time");
			var selector;
			if (unit === "ms") { selector = "delayMs"; }
			else if (unit === "s") { selector = "delayS"; }
			else if (unit === "m") { selector = "delayM"; }
			else {
				throw "Invalid delay unit: '" + unit + "'";
			}
			return builder.primitiveCall(id, selector, [time]);
		},
		start_task: function (block, ctx) {
			var id = XML.getId(block);
			var taskName = asIdentifier(XML.getChildNode(block, "taskName").innerText);
			return builder.start(id, [taskName]);
		},
		stop_task: function (block, ctx) {
			var id = XML.getId(block);
			var taskName = asIdentifier(XML.getChildNode(block, "taskName").innerText);
			return builder.stop(id, [taskName]);
		},
		resume_task: function (block, ctx) {
			var id = XML.getId(block);
			var taskName = asIdentifier(XML.getChildNode(block, "taskName").innerText);
			return builder.resume(id, [taskName]);
		},
		pause_task: function (block, ctx) {
			var id = XML.getId(block);
			var taskName = asIdentifier(XML.getChildNode(block, "taskName").innerText);
			return builder.pause(id, [taskName]);
		},
		run_task: function (block, ctx) {
			var id = XML.getId(block);
			var taskName = asIdentifier(XML.getChildNode(block, "taskName").innerText);
			return builder.scriptCall(id, taskName, []);
		},
		conditional_simple: function (block, ctx) {
			var id = XML.getId(block);
			var condition = generateCodeForValue(block, ctx, "condition");
			var trueBranch = generateCodeForStatements(block, ctx, "trueBranch");
			return builder.conditional(id, condition, trueBranch, []);
		},
		conditional_full: function (block, ctx) {
			var id = XML.getId(block);
			var condition = generateCodeForValue(block, ctx, "condition");
			var trueBranch = generateCodeForStatements(block, ctx, "trueBranch");
			var falseBranch = generateCodeForStatements(block, ctx, "falseBranch");
			return builder.conditional(id, condition, trueBranch, falseBranch);
		},
		logic_compare: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, ctx, "A");
			var right = generateCodeForValue(block, ctx, "B");
			var selector;
			if (type === "EQ") {
				selector = "==";
			} else if (type === "NEQ") {
				selector = "!=";
			} else if (type === "LT") {
				selector = "<";
			} else if (type === "LTE") {
				selector = "<=";
			} else if (type === "GTE") {
				selector = ">=";
			} else if (type === "GT") {
				selector = ">";
			} else {
				throw "Logical operator not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, [left, right]);
		},
		elapsed_time: function (block, ctx) {
			var id = XML.getId(block);
			var unit = XML.getChildNode(block, "unit").innerText;
			var selector;
			if (unit === "ms") {
				selector = "millis";
			} else if (unit === "s") {
				selector = "seconds";
			} else if (unit === "m") {
				selector = "minutes";
			}
			return builder.primitiveCall(id, selector, []);
		},
		toggle_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			return builder.primitiveCall(id, "toggle", [pinNumber]);
		},
		logical_operation: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "operator").innerText;
			var left = generateCodeForValue(block, ctx, "left");
			var right = generateCodeForValue(block, ctx, "right");
			if (type === "and") {
				return builder.logicalAnd(id, left, right);
			} else if (type === "or") {
				return builder.logicalOr(id, left, right);
			}
		},
		boolean: function (block, ctx) {
			var id = XML.getId(block);
			var bool = XML.getChildNode(block, "value").innerText;
			return builder.number(id, bool === "true" ? 1 : 0);
		},
		logical_not: function (block, ctx) {
			var id = XML.getId(block);
			var bool = generateCodeForValue(block, ctx, "value");
			return builder.primitiveCall(id, "!", [bool]);
		},
		number_property: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "property").innerText;
			var num = generateCodeForValue(block, ctx, "value");
			var args = [num];
			var selector;
			if (type === "even") {
				selector = "isEven";
			} else if (type === "odd") {
				selector = "isOdd";
			} else if (type === "prime") {
				selector = "isPrime";
			} else if (type === "whole") {
				selector = "isWhole";
			} else if (type === "positive") {
				selector = "isPositive";
			} else if (type === "negative") {
				selector = "isNegative";
			} else {
				throw "Math number property not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, args);
		},
		number_divisibility: function (block, ctx) {
			var id = XML.getId(block);
			var left = generateCodeForValue(block, ctx, "left");
			var right = generateCodeForValue(block, ctx, "right");
			selector = "isDivisibleBy";
			var args = [left, right];
			return builder.primitiveCall(id, selector, args);
		},
		repeat_times: function (block, ctx) {
			var id = XML.getId(block);
			var times = generateCodeForValue(block, ctx, "times");
			var statements = generateCodeForStatements(block, ctx);
			return builder.repeat(id, times, statements);
		},
		number_round: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "operator").innerText;
			var num = generateCodeForValue(block, ctx, "number");
			var valid = ["round", "ceil", "floor"];
			if (!valid.includes(type)) {
				throw "Math round type not found: '" + type + "'";
			}
			var selector = type;
			return builder.primitiveCall(id, selector, [num]);
		},
		number_operation: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "operator").innerText;
			var num = generateCodeForValue(block, ctx, "number");
			var selector;
			var args = [num];
			if (type === "sqrt") {
				selector = "sqrt";
			} else if (type === "abs") {
				selector = "abs";
			} else if (type === "negate") {
				selector = "*";
				args.push(builder.number(id, -1));
			} else if (type === "ln") {
				selector = "ln";
			} else if (type === "log10") {
				selector = "log10";
			} else if (type === "exp") {
				selector = "exp";
			} else if (type === "pow10") {
				selector = "pow10";
			} else {
				throw "Math function not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, args);
		},
		number_trig: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "operator").innerText;
			var num = generateCodeForValue(block, ctx, "number");
			var valid = ["sin", "cos", "tan", "asin", "acos", "atan"];
			if (!valid.includes(type)) {
				throw "Math trig function not found: '" + type + "'";
			}
			var selector = type;
			return builder.primitiveCall(id, selector, [num]);
		},
		math_constant: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "CONSTANT").innerText;
			var value;
			if (type === "PI") {
				value = Math.PI;
			} else if (type === "E") {
				value = Math.E;
			} else if (type === "GOLDEN_RATIO") {
				value = 1.61803398875;
			} else if (type === "SQRT2") {
				value = Math.SQRT2;
			} else if (type === "SQRT1_2") {
				value = Math.SQRT1_2;
			} else if (type === "INFINITY") {
				// HACK(Richo): Special case because JSON encodes Infinity as null
				value = {___INF___: 1};
			} else {
				throw "Math constant not found: '" + type + "'";
			}
			return builder.number(id, value);
		},
		math_arithmetic: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, ctx, "A");
			var right = generateCodeForValue(block, ctx, "B");
			var selector;
			if (type === "DIVIDE") {
				selector = "/";
			} else if (type === "MULTIPLY") {
				selector = "*";
			} else if (type === "MINUS") {
				selector = "-";
			} else if (type === "ADD") {
				selector = "+";
			} else if (type === "POWER") {
				selector = "**";
			} else {
				throw "Math arithmetic function not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, [left, right]);
		},
		timer: function (block, ctx) {
			var id = XML.getId(block);
			var taskName = asIdentifier(XML.getChildNode(block, "taskName").innerText);
			var runningTimes = parseFloat(XML.getChildNode(block, "runningTimes").innerText);
			var tickingScale = XML.getChildNode(block, "tickingScale").innerText;
			var initialState = XML.getChildNode(block, "initialState").innerText;
			var statements = generateCodeForStatements(block, ctx);
			return builder.task(id, taskName, [],
				initialState === "started" ? "running" : "stopped",
				builder.tickingRate(id, runningTimes, tickingScale),
				statements);
		},
		write_pin_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var pinValue = generateCodeForValue(block, ctx, "pinValue");
			return builder.primitiveCall(id, "write", [pinNumber, pinValue]);
		},
		read_pin_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			return builder.primitiveCall(id, "read", [pinNumber]);
		},
		degrees_servo_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var servoValue = generateCodeForValue(block, ctx, "servoValue");
			return builder.primitiveCall(id, "servoDegrees", [pinNumber, servoValue]);
		},
		repeat: function (block, ctx) {
			var id = XML.getId(block);
			var negated = XML.getChildNode(block, "negate").innerText === "true";
			var condition = generateCodeForValue(block, ctx, "condition");
			var statements = generateCodeForStatements(block, ctx);
			return builder.while(id, condition, statements, negated);
		},
		is_pin_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinState = XML.getChildNode(block, "pinState").innerText;
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var selector = pinState === "on" ? "isOn" : "isOff";
			return builder.primitiveCall(id, selector, [pinNumber]);
		},
		wait: function (block, ctx) {
			var id = XML.getId(block);
			var negated = XML.getChildNode(block, "negate").innerText === "true";
			var condition = generateCodeForValue(block, ctx, "condition");
			return builder.while(id, condition, [], negated);
		},
		number_modulo: function (block, ctx) {
			var id = XML.getId(block);
			var left = generateCodeForValue(block, ctx, "dividend");
			var right = generateCodeForValue(block, ctx, "divisor");
			return builder.primitiveCall(id, "%", [left, right]);
		},
		set_variable: function (block, ctx) {
			var id = XML.getId(block);
			var name = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			if (!ctx.isLocalDefined(name)) {
				ctx.addGlobal(name);
			}
			var value = generateCodeForValue(block, ctx, "value");
			if (value == undefined) {
				value = builder.number(id, 0);
			}
			return builder.assignment(id, name, value);
		},
		increment_variable: function (block, ctx) {
			var id = XML.getId(block);
			var name = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			if (!ctx.isLocalDefined(name)) {
				ctx.addGlobal(name);
			}
			var delta = generateCodeForValue(block, ctx, "value");
			var variable = builder.variable(id, name);
			return builder.assignment(id, name,
				builder.primitiveCall(id, "+", [variable, delta]));
		},
		number_constrain: function (block, ctx) {
			var id = XML.getId(block);
			var value = generateCodeForValue(block, ctx, "value");
			var low = generateCodeForValue(block, ctx, "low");
			var high = generateCodeForValue(block, ctx, "high");
			return builder.primitiveCall(id, "constrain", [value, low, high]);
		},
		number_random_int: function (block, ctx) {
			var id = XML.getId(block);
			var from = generateCodeForValue(block, ctx, "from");
			var to = generateCodeForValue(block, ctx, "to");
			return builder.primitiveCall(id, "randomInt", [from, to]);
		},
		number_random_float: function (block, ctx) {
			var id = XML.getId(block);
			return builder.primitiveCall(id, "random", []);
		},
		procedures_defnoreturn: function (block, ctx) {
			var id = XML.getId(block);
			var name = "_" + asIdentifier(XML.getChildNode(block, "NAME").innerText);
			var mutation = XML.getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var args = [];
			if (mutation !== undefined) {
				mutation.childNodes.forEach(function (each) {
					args.push(asIdentifier(each.getAttribute("name")));
				});
			}
			var statements = generateCodeForStatements(block, ctx, "STACK");
			return builder.procedure(id, name, args, statements);
		},
		comment_statement: function (block, ctx) {
			return undefined;
		},
		procedures_callnoreturn: function (block, ctx) {
			var id = XML.getId(block);
			var mutation = XML.getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var scriptName = "_" + asIdentifier(mutation.getAttribute("name"));
			var argNames = [];
			mutation.childNodes.forEach(function (each) {
				argNames.push(asIdentifier(each.getAttribute("name")));
			});
			var args = [];
			for (var i = 0; i < argNames.length; i++) {
				var value = generateCodeForValue(block, ctx, "ARG" + i);
				var name = argNames[i];
				args.push({ name: name, value: value });
			}
			return builder.scriptCall(id, scriptName, args);
		},
		procedures_callreturn: function (block, ctx) {
			var id = XML.getId(block);
			var mutation = XML.getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var scriptName = "_" + asIdentifier(mutation.getAttribute("name"));
			var argNames = [];
			mutation.childNodes.forEach(function (each) {
				argNames.push(asIdentifier(each.getAttribute("name")));
			});
			var args = [];
			for (var i = 0; i < argNames.length; i++) {
				var value = generateCodeForValue(block, ctx, "ARG" + i);
				var name = argNames[i];
				args.push({ name: name, value: value });
			}
			return builder.scriptCall(id, scriptName, args);
		},
		procedures_ifreturn: function (block, ctx) {
			var id = XML.getId(block);
			var condition = generateCodeForValue(block, ctx, "CONDITION");
			var value = generateCodeForValue(block, ctx, "VALUE");
			return builder.conditional(id,
				condition,
				[builder.return(id, value || null)],
				[]);
		},
		procedures_defreturn: function (block, ctx) {
			var id = XML.getId(block);
			var name = "_" + asIdentifier(XML.getChildNode(block, "NAME").innerText);
			var mutation = XML.getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var args = [];
			if (mutation !== undefined) {
				mutation.childNodes.forEach(function (each) {
					args.push(asIdentifier(each.getAttribute("name")));
				});
			}
			var statements = generateCodeForStatements(block, ctx, "STACK");
			// TODO(Richo): Decide what to do if the return block is not defined
			var returnExpr = generateCodeForValue(block, ctx, "RETURN");
			statements.push(builder.return(id, returnExpr));
			return builder.function(id, name, args, statements);
		},
		comment_expression: function (block, ctx) {
			return generateCodeForValue(block, ctx, "NAME");
		},
		pin: function (block, ctx) {
			var id = XML.getId(block);
			var pin = XML.getChildNode(block, "pinNumber").innerText;
			var type = pin[0];
			var number = parseInt(pin.slice(1));
			return builder.pin(id, type, number);
		},
		move_dcmotor: function (block, ctx) {
			var id = XML.getId(block);
			var motorName = asIdentifier(XML.getChildNode(block, "motorName").innerText);
			var direction = XML.getChildNode(block, "direction").innerText;
			var speed = generateCodeForValue(block, ctx, "speed");

			ctx.addDCMotorImport(motorName);

			let selector = motorName + "." + (direction == "fwd" ? "forward" : "backward");
			let arg = {name: "speed", value: speed};
			return builder.scriptCall(id, selector, [arg]);
		},
		change_speed_dcmotor: function (block, ctx) {
			var id = XML.getId(block);
			var motorName = asIdentifier(XML.getChildNode(block, "motorName").innerText);
			var speed = generateCodeForValue(block, ctx, "speed");

			ctx.addDCMotorImport(motorName);

			let selector = motorName + "." + "setSpeed";
			let arg = {name: "speed", value: speed};
			return builder.scriptCall(id, selector, [arg]);
		},
		stop_dcmotor: function (block, ctx) {
			var id = XML.getId(block);
			var motorName = asIdentifier(XML.getChildNode(block, "motorName").innerText);

			ctx.addDCMotorImport(motorName);

			let selector = motorName + "." + "brake";
			return builder.scriptCall(id, selector, []);
		},
		get_sonar_distance: function (block, ctx) {
			var id = XML.getId(block);
			var sonarName = asIdentifier(XML.getChildNode(block, "sonarName").innerText);
			var unit = XML.getChildNode(block, "unit").innerText;

			ctx.addSonarImport(sonarName);

			let selector = sonarName + "." + "distance_" + unit;
			return builder.scriptCall(id, selector, []);
		},
	};

	function asIdentifier(str) {
		return str.replace(/ /g, '_');
	}

	function generateCodeFor(block, ctx) {
		if (isDisabled(block)) return undefined;

		var type = block.getAttribute("type");
		var func = dispatchTable[type];
		if (func == undefined) {
			throw "CODEGEN ERROR: Type not found '" + type + "'";
		}
		try {
			ctx.path.push(block);
			return func(block, ctx);
		}
		finally {
			ctx.path.pop();
		}
	}

	function generateCodeForValue(block, ctx, name) {
		var child = XML.getChildNode(block, name);
		if (child === undefined) return undefined;
		return generateCodeFor(XML.getLastChild(child), ctx);
	}

	function generateCodeForStatements(block, ctx, name) {
		var statements = [];
		var child = XML.getChildNode(block, name || "statements");
		if (child !== undefined) {
			child.childNodes.forEach(function (each) {
				var next = each;
				do {
					var code = generateCodeFor(next, ctx);
					if (code !== undefined) {
						statements.push(code);
					}
					next = getNextStatement(next);
				} while (next !== undefined);
			});
		}
		return statements;
	}

	function getNextStatement(block) {
		var next = XML.getLastChild(block, function (child) {
			return child.tagName === "NEXT";
		});
		if (next === undefined) { return next; }
		return next.childNodes[0];
	}

	function isTopLevel(block) {
		return topLevelBlocks.indexOf(block.getAttribute("type")) != -1;
	}

	function isDisabled(block) {
		return block.getAttribute("disabled") === "true";
	}

	return {
		generate: function (xml, motors, sonars) {
			var setup = [];
			var scripts = [];
			var ctx = {
				path: [xml],
				imports: new Map(),
				globals: [],

				isLocalDefined: function (name) {
					/*
					 * NOTE(Richo): For now, the only block capable of declaring local variables
					 * is the "for". So, we simply filter our path looking for "for" blocks and
					 * then we check if any of them define a variable with the specified name.
					 */
					let blocks = ctx.path.filter(function (b) { return b.getAttribute("type") == "for"; });
					return blocks.some(function (b) {
						let field = XML.getChildNode(b, "variable");
						return field != undefined && field.innerText == name;
					});
				},

				addDCMotorImport: function (alias) {
					ctx.addImport(alias, "DCMotor.uzi", function () {
						let motor = motors.find(function (m) { return m.name === alias; });
						if (motor == undefined) return null;

						function pin(pin) {
							var type = pin[0];
							var number = parseInt(pin.slice(1));
							return builder.pin(null, type, number);
						}

						let stmts = [];
						stmts.push(builder.assignment(null, "enablePin", pin(motor.enable)));
						stmts.push(builder.assignment(null, "forwardPin", pin(motor.fwd)));
						stmts.push(builder.assignment(null, "reversePin", pin(motor.bwd)));
						return builder.block(null, stmts);
					});
				},
				addSonarImport: function (alias) {
					ctx.addImport(alias, "Sonar.uzi", function () {
						let sonar = sonars.find(function (m) { return m.name === alias; });
						if (sonar == undefined) return null;

						function pin(pin) {
							var type = pin[0];
							var number = parseInt(pin.slice(1));
							return builder.pin(null, type, number);
						}

						let stmts = [];
						stmts.push(builder.assignment(null, "trigPin", pin(sonar.trig)));
						stmts.push(builder.assignment(null, "echoPin", pin(sonar.echo)));
						stmts.push(builder.assignment(null, "maxDistance", builder.number(null, parseInt(sonar.maxDist))));
						stmts.push(builder.start(null, ["reading"]));
						return builder.block(null, stmts);
					});
				},
				addImport: function (alias, path, initFn) {
					if (ctx.imports.has(alias)) return false;

					ctx.imports.set(alias, builder.import(null, alias, path, initFn()));
					return true;
				},
				addGlobal: function (varName) {
					if (!ctx.globals.includes(varName)) {
						ctx.globals.push(varName);
					}
				}
			};
			xml.childNodes.forEach(function (block) {
				if (isTopLevel(block)) {
					var code = generateCodeFor(block, ctx);
					if (code !== undefined) {
						scripts.push(code);
					}
				}
			});
			if (setup.length > 0) {
				let name = "setup";
				while (scripts.find(function (s) { return s.name === name; }) != undefined) {
					name = "_" + name;
				}
				scripts.unshift(builder.task(null, name, [], "once", null, setup));
			}
			return builder.program(null,
				Array.from(ctx.imports, function (entry) { return entry[1]; }),
				ctx.globals,
				scripts);
		}
	}
})();
