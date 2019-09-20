var BlocksToAST = (function () {

	var builder = {
		program: function (id, imports, globals, scripts) {
			return {
				__class__: "UziProgramNode",
				imports: Array.from(imports, function (entry) {
					let alias = entry[0];
					let path = entry[1];
					return builder.import(id, alias, path);
				}),
				globals: globals.map(function (varName) {
					return builder.variableDeclaration(id, varName);
				}),
				scripts: scripts
			};
		},
		import: function (id, alias, path) {
			return {
				__class__: "UziImportNode",
				alias: alias,
				path: path
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
		primitiveCall: function (id, selector, args, primitiveName) {
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
				}),
				primitiveName: primitiveName || selector
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
		math_number: function (block, ctx) {
			var id = XML.getId(block);
			var value = parseFloat(XML.getChildNode(block, "NUM").innerText);
			return builder.number(id, value);
		},
		turn_pin_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinState = XML.getChildNode(block, "pinState").innerText;
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var selector = pinState === "on" ? "turnOn" : "turnOff";
			return builder.primitiveCall(id, selector, [pinNumber]);
		},
		variables_get: function (block, ctx) {
			var id = XML.getId(block);
			var variableName = asIdentifier(XML.getChildNode(block, "VAR").innerText);
			ctx.addGlobal(variableName);
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
			var selector, primName;
			if (type === "EQ") {
				selector = "==";
				primName = "equals";
			} else if (type === "NEQ") {
				selector = "!=";
				primName = "notEquals";
			} else if (type === "LT") {
				selector = "<";
				primName = "lessThan";
			} else if (type === "LTE") {
				selector = "<=";
				primName = "lessThanOrEquals";
			} else if (type === "GTE") {
				selector = ">=";
				primName = "greaterThanOrEquals";
			} else if (type === "GT") {
				selector = ">";
				primName = "greaterThan";
			} else {
				throw "Logical operator not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, [left, right], primName);
		},
		elapsed_time: function (block, ctx) {
			var id = XML.getId(block);
			var unit = XML.getChildNode(block, "unit").innerText;
			var selector, primName;
			if (unit === "ms") {
				selector = "millis";
				primName = "millis";
			} else if (unit === "s") {
				selector = "seconds";
				primName = "seconds";
			} else if (unit === "m") {
				selector = "minutes";
				primName = "minutes";
			}
			return builder.primitiveCall(id, selector, [], primName);
		},
		toggle_variable: function (block, ctx) {
			var id = XML.getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			return builder.primitiveCall(id, "toggle", [pinNumber]);
		},
		logic_operation: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, ctx, "A");
			var right = generateCodeForValue(block, ctx, "B");
			if (type === "AND") {
				return builder.logicalAnd(id, left, right);
			} else if (type === "OR") {
				return builder.logicalOr(id, left, right);
			}
		},
		logic_boolean: function (block, ctx) {
			var id = XML.getId(block);
			var bool = XML.getChildNode(block, "BOOL").innerText;
			return builder.number(id, bool === "TRUE" ? 1 : 0);
		},
		logic_negate: function (block, ctx) {
			var id = XML.getId(block);
			var bool = generateCodeForValue(block, ctx, "BOOL");
			return builder.primitiveCall(id, "!", [bool], "negate");
		},
		math_number_property: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "PROPERTY").innerText;
			var num = generateCodeForValue(block, ctx, "NUMBER_TO_CHECK");
			var args = [num];
			var selector, primName;
			if (type === "EVEN") {
				selector = "isEven";
				primName = "isEven";
			} else if (type === "ODD") {
				selector = "isOdd";
				primName = "isOdd";
			} else if (type === "PRIME") {
				selector = "isPrime";
				primName = "isPrime";
			} else if (type === "WHOLE") {
				selector = "isWhole";
				primName = "isWhole";
			} else if (type === "POSITIVE") {
				selector = "isPositive";
				primName = "isPositive";
			} else if (type === "NEGATIVE") {
				selector = "isNegative";
				primName = "isNegative";
			} else if (type === "DIVISIBLE_BY") {
				selector = "isDivisibleBy";
				primName = "isDivisibleBy";
				var divisor = generateCodeForValue(block, ctx, "DIVISOR");
				args.push(divisor);
			} else {
				throw "Math number property not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, args, primName);
		},
		repeat_times: function (block, ctx) {
			var id = XML.getId(block);
			var times = generateCodeForValue(block, ctx, "times");
			var statements = generateCodeForStatements(block, ctx);
			return builder.repeat(id, times, statements);
		},
		math_round: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "OP").innerText;
			var num = generateCodeForValue(block, ctx, "NUM");
			var selector, primName;
			if (type === "ROUND") {
				selector = "round";
				primName = "round";
			} else if (type === "ROUNDUP") {
				selector = "ceil";
				primName = "ceil";
			} else if (type === "ROUNDDOWN") {
				selector = "floor";
				primName = "floor";
			} else {
				throw "Math round type not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, [num], primName);
		},
		math_single: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "OP").innerText;
			var num = generateCodeForValue(block, ctx, "NUM");
			var selector, primName;
			var args = [num];
			if (type === "ROOT") {
				selector = "sqrt";
				primName = "sqrt";
			} else if (type === "ABS") {
				selector = "abs";
				primName = "abs";
			} else if (type === "NEG") {
				selector = "*";
				primName = "multiply";
				args.push(builder.number(id, -1));
			} else if (type === "LN") {
				selector = "ln";
				primName = "ln";
			} else if (type === "LOG10") {
				selector = "log10";
				primName = "log10";
			} else if (type === "EXP") {
				selector = "exp";
				primName = "exp";
			} else if (type === "POW10") {
				selector = "pow10";
				primName = "pow10";
			} else {
				throw "Math function not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, args, primName);
		},
		math_trig: function (block, ctx) {
			var id = XML.getId(block);
			var type = XML.getChildNode(block, "OP").innerText;
			var num = generateCodeForValue(block, ctx, "NUM");
			var selector, primName;
			if (type === "SIN") {
				selector = "sin";
				primName = "sin";
			} else if (type === "COS") {
				selector = "cos";
				primName = "cos";
			} else if (type === "TAN") {
				selector = "tan";
				primName = "tan";
			} else if (type === "ASIN") {
				selector = "asin";
				primName = "asin";
			} else if (type === "ACOS") {
				selector = "acos";
				primName = "acos";
			} else if (type === "ATAN") {
				selector = "atan";
				primName = "atan";
			} else {
				throw "Math trig function not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, [num], primName);
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
			var selector, primName;
			if (type === "DIVIDE") {
				selector = "/";
				primName = "divide";
			} else if (type === "MULTIPLY") {
				selector = "*";
				primName = "multiply";
			} else if (type === "MINUS") {
				selector = "-";
				primName = "subtract";
			} else if (type === "ADD") {
				selector = "+";
				primName = "add";
			} else if (type === "POWER") {
				selector = "**";
				primName = "power";
			} else {
				throw "Math arithmetic function not found: '" + type + "'";
			}
			return builder.primitiveCall(id, selector, [left, right], primName);
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
			var selector, primName;
			primName = selector = pinState === "on" ? "isOn" : "isOff";
			return builder.primitiveCall(id, selector, [pinNumber], primName);
		},
		wait: function (block, ctx) {
			var id = XML.getId(block);
			var negated = XML.getChildNode(block, "negate").innerText === "true";
			var condition = generateCodeForValue(block, ctx, "condition");
			return builder.while(id, condition, [], negated);
		},
		math_modulo: function (block, ctx) {
			var id = XML.getId(block);
			var left = generateCodeForValue(block, ctx, "DIVIDEND");
			var right = generateCodeForValue(block, ctx, "DIVISOR");
			return builder.primitiveCall(id, "%", [left, right], "mod");
		},
		variables_set: function (block, ctx) {
			var id = XML.getId(block);
			var name = asIdentifier(XML.getChildNode(block, "VAR").innerText);
			ctx.addGlobal(name);
			var value = generateCodeForValue(block, ctx, "VALUE");
			return builder.assignment(id, name, value);
		},
		math_change: function (block, ctx) {
			var id = XML.getId(block);
			var name = asIdentifier(XML.getChildNode(block, "VAR").innerText);
			ctx.addGlobal(name);
			var delta = generateCodeForValue(block, ctx, "DELTA");
			var variable = builder.variable(id, name);
			return builder.assignment(id, name,
				builder.primitiveCall(id, "+", [variable, delta], "add"));
		},
		math_constrain: function (block, ctx) {
			var id = XML.getId(block);
			var value = generateCodeForValue(block, ctx, "VALUE");
			var low = generateCodeForValue(block, ctx, "LOW");
			var high = generateCodeForValue(block, ctx, "HIGH");
			return builder.primitiveCall(id, "constrain", [value, low, high]);
		},
		math_random_int: function (block, ctx) {
			var id = XML.getId(block);
			var from = generateCodeForValue(block, ctx, "FROM");
			var to = generateCodeForValue(block, ctx, "TO");
			return builder.primitiveCall(id, "randomInt", [from, to]);
		},
		math_random_float: function (block, ctx) {
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
		}
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
		generate: function (xml, motors) {
			var setup = [];
			var scripts = [];
			var ctx = {
				path: [xml],
				imports: new Map(),
				globals: [],

				addDCMotorImport: function (alias) {
					if (ctx.addImport(alias, "DCMotor.uzi")) {

						// Initialize motor
						let motor = motors.find(function (m) { return m.name === alias; });
						if (motor != undefined) {
							let selector = motor.name + "." + "init";

							function pin(pin) {
								var type = pin[0];
								var number = parseInt(pin.slice(1));
								return builder.pin(null, type, number);
							}
							let args = [
								{ name: "en", value: pin(motor.enable) },
								{ name: "f", value: pin(motor.fwd) },
								{ name: "r", value: pin(motor.bwd) },
							];

							setup.push(builder.scriptCall(null, selector, args));
						}
					}
				},
				addImport: function (alias, path) {
					if (ctx.imports.has(alias)) return false;
					ctx.imports.set(alias, path);
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
			return builder.program(null, ctx.imports, ctx.globals, scripts);
		}
	}
})();
