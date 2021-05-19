let BlocksToAST = (function () {

	let builder = {
		program: function (id, imports, globals, scripts) {
			return {
				__class__: "UziProgramNode",
				imports: imports,
				globals: globals.filter(g => g != undefined).map(g => {
					let name = g.name;
					let value = g.value ? parseFloat(g.value) : 0;
					return builder.variableDeclaration(id, name, builder.number(id, value));
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
		yield: function (id) {
			return {
				__class__: "UziYieldNode",
				id: id,
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
		variableDeclaration: function (id, name, value) {
			return {
				__class__: "UziVariableDeclarationNode",
				id: id,
				name: name,
				value: value || builder.number(id, 0)
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
		while: function (id, condition, statements) {
			return {
				__class__: "UziWhileNode",
				id: id,
				pre: builder.block(id, []),
				condition: condition,
				post: builder.block(id, statements),
				negated: false
			};
		},
		until: function (id, condition, statements) {
			return {
				__class__: "UziUntilNode",
				id: id,
				pre: builder.block(id, []),
				condition: condition,
				post: builder.block(id, statements),
				negated: true
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

	// TODO(Richo): The spec now has this info
	let topLevelBlocks = ["task", "timer",
												"import", "here_be_dragons_script",
												"proc_definition_0args", "proc_definition_1args",
												"proc_definition_2args", "proc_definition_3args",
												"func_definition_0args", "func_definition_1args",
												"func_definition_2args", "func_definition_3args"];
	let dispatchTable =  {
		// TODO(Richo)
		here_be_dragons_stmt: function (block, ctx, stream) {
			let node = JSONX.parse(block.children[0].textContent);
			stream.push(node);
		},
		here_be_dragons_expr: function (block, ctx, stream) {
			let node = JSONX.parse(block.children[0].textContent);
			stream.push(node);
		},
		here_be_dragons_script: function (block, ctx, stream) {
			let node = JSONX.parse(block.children[0].textContent);
			stream.push(node);
		},
		import: function (block, ctx, stream) {
			let alias = XML.getChildNode(block, "alias").innerText;
			let path = XML.getChildNode(block, "path").innerText;

			// NOTE(Richo): Instead of creating the block here we just add the import to the ctx
			ctx.addImport(alias, path, function () {
				return JSONX.parse(XML.getLastChild(block, n => n.tagName == "COMMENT").textContent);
			});
		},
		yield: function (block, ctx, stream) {
			let id = XML.getId(block);
			stream.push(builder.yield(id));
		},
		task: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			stream.push(builder.task(id, scriptName, [], "once", null, statements));
		},
		forever: function (block, ctx, stream) {
			let id = XML.getId(block);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			stream.push(builder.forever(id, statements));
		},
		for: function (block, ctx, stream) {
			let id = XML.getId(block);
			let variableName = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			let start = generateCodeForValue(block, ctx, "start");
			let stop = generateCodeForValue(block, ctx, "stop");
			let step = generateCodeForValue(block, ctx, "step");
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			stream.push(builder.for(id, variableName, start, stop, step, statements));
		},
		number: function (block, ctx, stream) {
			let id = XML.getId(block);
			let value = parseFloat(XML.getChildNode(block, "value").innerText);
			stream.push(builder.number(id, value));
		},
		turn_onoff_pin: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinState = XML.getChildNode(block, "pinState").innerText;
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let selector = pinState === "on" ? "turnOn" : "turnOff";
			stream.push(builder.primitiveCall(id, selector, [pinNumber]));
		},
		variable: function (block, ctx, stream) {
			let id = XML.getId(block);
			let variableName = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			if (!ctx.isLocalDefined(variableName)) {
				ctx.addGlobal(variableName);
			}
			stream.push(builder.variable(id, variableName));
		},
		delay: function (block, ctx, stream) {
			let id = XML.getId(block);
			let unit = XML.getChildNode(block, "unit").innerText;
			let time = generateCodeForValue(block, ctx, "time");
			let selector;
			if (unit === "ms") { selector = "delayMs"; }
			else if (unit === "s") { selector = "delayS"; }
			else if (unit === "m") { selector = "delayM"; }
			else {
				throw "Invalid delay unit: '" + unit + "'";
			}
			stream.push(builder.primitiveCall(id, selector, [time]));
		},
		start_task: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			stream.push(builder.start(id, [scriptName]));
		},
		stop_task: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			stream.push(builder.stop(id, [scriptName]));
		},
		resume_task: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			stream.push(builder.resume(id, [scriptName]));
		},
		pause_task: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			stream.push(builder.pause(id, [scriptName]));
		},
		run_task: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			stream.push(builder.scriptCall(id, scriptName, []));
		},
		conditional_simple: function (block, ctx, stream) {
			let id = XML.getId(block);
			let condition = generateCodeForValue(block, ctx, "condition");
			let trueBranch = [];
			generateCodeForStatements(block, ctx, "trueBranch", trueBranch);
			stream.push(builder.conditional(id, condition, trueBranch, []));
		},
		conditional_full: function (block, ctx, stream) {
			let id = XML.getId(block);
			let condition = generateCodeForValue(block, ctx, "condition");
			let trueBranch = [];
			generateCodeForStatements(block, ctx, "trueBranch", trueBranch);
			let falseBranch = [];
			generateCodeForStatements(block, ctx, "falseBranch", falseBranch);
			stream.push(builder.conditional(id, condition, trueBranch, falseBranch));
		},
		logical_compare: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "operator").innerText;
			let left = generateCodeForValue(block, ctx, "left");
			let right = generateCodeForValue(block, ctx, "right");
			let valid = ["==", "!=", "<", "<=", ">", ">="];
			if (!valid.includes(type)) {
				throw "Logical operator not found: '" + type + "'";
			}
			let selector = type;
			stream.push(builder.primitiveCall(id, selector, [left, right]));
		},
		elapsed_time: function (block, ctx, stream) {
			let id = XML.getId(block);
			let unit = XML.getChildNode(block, "unit").innerText;
			let selector;
			if (unit === "ms") {
				selector = "millis";
			} else if (unit === "s") {
				selector = "seconds";
			} else if (unit === "m") {
				selector = "minutes";
			}
			stream.push(builder.primitiveCall(id, selector, []));
		},
		toggle_pin: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			stream.push(builder.primitiveCall(id, "toggle", [pinNumber]));
		},
		logical_operation: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "operator").innerText;
			let left = generateCodeForValue(block, ctx, "left");
			let right = generateCodeForValue(block, ctx, "right");
			if (type === "and") {
				stream.push(builder.logicalAnd(id, left, right));
			} else if (type === "or") {
				stream.push(builder.logicalOr(id, left, right));
			}
		},
		boolean: function (block, ctx, stream) {
			let id = XML.getId(block);
			let bool = XML.getChildNode(block, "value").innerText;
			stream.push(builder.number(id, bool === "true" ? 1 : 0));
		},
		logical_not: function (block, ctx, stream) {
			let id = XML.getId(block);
			let bool = generateCodeForValue(block, ctx, "value");
			stream.push(builder.primitiveCall(id, "!", [bool]));
		},
		number_property: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "property").innerText;
			let num = generateCodeForValue(block, ctx, "value");
			let args = [num];
			let selector;
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
			stream.push(builder.primitiveCall(id, selector, args));
		},
		number_divisibility: function (block, ctx, stream) {
			let id = XML.getId(block);
			let left = generateCodeForValue(block, ctx, "left");
			let right = generateCodeForValue(block, ctx, "right");
			selector = "isDivisibleBy";
			let args = [left, right];
			stream.push(builder.primitiveCall(id, selector, args));
		},
		repeat_times: function (block, ctx, stream) {
			let id = XML.getId(block);
			let times = generateCodeForValue(block, ctx, "times");
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			stream.push(builder.repeat(id, times, statements));
		},
		number_round: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "operator").innerText;
			let num = generateCodeForValue(block, ctx, "number");
			let valid = ["round", "ceil", "floor"];
			if (!valid.includes(type)) {
				throw "Math round type not found: '" + type + "'";
			}
			let selector = type;
			stream.push(builder.primitiveCall(id, selector, [num]));
		},
		number_operation: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "operator").innerText;
			let num = generateCodeForValue(block, ctx, "number");
			let selector;
			let args = [num];
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
			stream.push(builder.primitiveCall(id, selector, args));
		},
		number_trig: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "operator").innerText;
			let num = generateCodeForValue(block, ctx, "number");
			let valid = ["sin", "cos", "tan", "asin", "acos", "atan"];
			if (!valid.includes(type)) {
				throw "Math trig function not found: '" + type + "'";
			}
			let selector = type;
			stream.push(builder.primitiveCall(id, selector, [num]));
		},
		math_constant: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "constant").innerText;
			let value;
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
			stream.push(builder.number(id, value));
		},
		math_arithmetic: function (block, ctx, stream) {
			let id = XML.getId(block);
			let type = XML.getChildNode(block, "operator").innerText;
			let left = generateCodeForValue(block, ctx, "left");
			let right = generateCodeForValue(block, ctx, "right");
			let selector;
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
			stream.push(builder.primitiveCall(id, selector, [left, right]));
		},
		timer: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let runningTimes = parseFloat(XML.getChildNode(block, "runningTimes").innerText);
			let tickingScale = XML.getChildNode(block, "tickingScale").innerText;
			let initialState = XML.getChildNode(block, "initialState").innerText;
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			stream.push(builder.task(id, scriptName, [],
				initialState === "started" ? "running" : "stopped",
				builder.tickingRate(id, runningTimes, tickingScale),
				statements));
		},
		write_pin: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let pinValue = generateCodeForValue(block, ctx, "pinValue");
			stream.push(builder.primitiveCall(id, "write", [pinNumber, pinValue]));
		},
		set_pin_mode: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let validModes = {
				INPUT: 0,
				OUTPUT: 1,
				INPUT_PULLUP: 2
			};
			let mode = XML.getChildNode(block, "mode").innerText;
			let actualMode = validModes[mode];
			if (actualMode != undefined) {
				actualMode = builder.number(id, actualMode);
			} else {
				throw "Invalid pin mode: '" + mode + "'";
			}
			stream.push(builder.primitiveCall(id, "setPinMode", [pinNumber, actualMode]));
		},
		read_pin: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			stream.push(builder.primitiveCall(id, "read", [pinNumber]));
		},
		get_servo_degrees: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			stream.push(builder.primitiveCall(id, "getServoDegrees", [pinNumber]));
		},
		set_servo_degrees: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let servoValue = generateCodeForValue(block, ctx, "servoValue");
			stream.push(builder.primitiveCall(id, "setServoDegrees", [pinNumber, servoValue]));
		},
		repeat: function (block, ctx, stream) {
			let id = XML.getId(block);
			let negated = XML.getChildNode(block, "negate").innerText === "true";
			let condition = generateCodeForValue(block, ctx, "condition");
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			if (negated) {
				stream.push(builder.until(id, condition, statements));
			} else {
				stream.push(builder.while(id, condition, statements));
			}
		},
		is_onoff_pin: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pinState = XML.getChildNode(block, "pinState").innerText;
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let selector = pinState === "on" ? "isOn" : "isOff";
			stream.push(builder.primitiveCall(id, selector, [pinNumber]));
		},
		wait: function (block, ctx, stream) {
			let id = XML.getId(block);
			let negated = XML.getChildNode(block, "negate").innerText === "true";
			let condition = generateCodeForValue(block, ctx, "condition");
			if (negated) {
				stream.push(builder.until(id, condition, []));
			} else {
				stream.push(builder.while(id, condition, []));
			}
		},
		number_modulo: function (block, ctx, stream) {
			let id = XML.getId(block);
			let left = generateCodeForValue(block, ctx, "dividend");
			let right = generateCodeForValue(block, ctx, "divisor");
			stream.push(builder.primitiveCall(id, "%", [left, right]));
		},
		set_variable: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			if (!ctx.isLocalDefined(name)) {
				ctx.addGlobal(name);
			}
			let value = generateCodeForValue(block, ctx, "value");
			if (value == undefined) {
				value = builder.number(id, 0);
			}
			stream.push(builder.assignment(id, name, value));
		},
		increment_variable: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			if (!ctx.isLocalDefined(name)) {
				ctx.addGlobal(name);
			}
			let delta = generateCodeForValue(block, ctx, "value");
			let variable = builder.variable(id, name);
			stream.push(builder.assignment(id, name,
				builder.primitiveCall(id, "+", [variable, delta])));
		},
		number_constrain: function (block, ctx, stream) {
			let id = XML.getId(block);
			let value = generateCodeForValue(block, ctx, "value");
			let low = generateCodeForValue(block, ctx, "low");
			let high = generateCodeForValue(block, ctx, "high");
			stream.push(builder.primitiveCall(id, "constrain", [value, low, high]));
		},
		number_between: function (block, ctx, stream) {
			let id = XML.getId(block);
			let args = [
				{name: "value", value: generateCodeForValue(block, ctx, "value")},
				{name: "min", value: generateCodeForValue(block, ctx, "low")},
				{name: "max", value: generateCodeForValue(block, ctx, "high")}];
			stream.push(builder.scriptCall(id, "isBetween", args));
		},
		number_random_int: function (block, ctx, stream) {
			let id = XML.getId(block);
			let from = generateCodeForValue(block, ctx, "from");
			let to = generateCodeForValue(block, ctx, "to");
			stream.push(builder.primitiveCall(id, "randomInt", [from, to]));
		},
		number_random_float: function (block, ctx, stream) {
			let id = XML.getId(block);
			stream.push(builder.primitiveCall(id, "random", []));
		},
		pin: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pin = XML.getChildNode(block, "pinNumber").innerText;
			let type = pin[0];
			let number = parseInt(pin.slice(1));
			stream.push(builder.pin(id, type, number));
		},
		move_dcmotor: function (block, ctx, stream) {
			let id = XML.getId(block);
			let motorName = asIdentifier(XML.getChildNode(block, "motorName").innerText);
			let direction = XML.getChildNode(block, "direction").innerText;
			let speed = generateCodeForValue(block, ctx, "speed");

			ctx.addDCMotorImport(motorName);

			let selector = motorName + "." + (direction == "fwd" ? "forward" : "backward");
			let arg = {name: "speed", value: speed};
			stream.push(builder.scriptCall(id, selector, [arg]));
		},
		change_speed_dcmotor: function (block, ctx, stream) {
			let id = XML.getId(block);
			let motorName = asIdentifier(XML.getChildNode(block, "motorName").innerText);
			let speed = generateCodeForValue(block, ctx, "speed");

			ctx.addDCMotorImport(motorName);

			let selector = motorName + "." + "setSpeed";
			let arg = {name: "speed", value: speed};
			stream.push(builder.scriptCall(id, selector, [arg]));
		},
		get_speed_dcmotor: function (block, ctx, stream) {
			let id = XML.getId(block);
			let motorName = asIdentifier(XML.getChildNode(block, "motorName").innerText);

			ctx.addDCMotorImport(motorName);

			let selector = motorName + "." + "getSpeed";
			stream.push(builder.scriptCall(id, selector, []));
		},
		stop_dcmotor: function (block, ctx, stream) {
			let id = XML.getId(block);
			let motorName = asIdentifier(XML.getChildNode(block, "motorName").innerText);

			ctx.addDCMotorImport(motorName);

			let selector = motorName + "." + "brake";
			stream.push(builder.scriptCall(id, selector, []));
		},
		get_sonar_distance: function (block, ctx, stream) {
			let id = XML.getId(block);
			let sonarName = asIdentifier(XML.getChildNode(block, "sonarName").innerText);
			let unit = XML.getChildNode(block, "unit").innerText;

			ctx.addSonarImport(sonarName);

			let selector = sonarName + "." + "distance_" + unit;
			stream.push(builder.scriptCall(id, selector, []));
		},
		get_joystick_x: function (block, ctx, stream) {
			let id = XML.getId(block);
			let joystickName = asIdentifier(XML.getChildNode(block, "joystickName").innerText);

			ctx.addJoystickImport(joystickName);

			let variableName = joystickName + "." + "x";
			stream.push(builder.variable(id, variableName));
		},
		get_joystick_y: function (block, ctx, stream) {
			let id = XML.getId(block);
			let joystickName = asIdentifier(XML.getChildNode(block, "joystickName").innerText);

			ctx.addJoystickImport(joystickName);

			let variableName = joystickName + "." + "y";
			stream.push(builder.variable(id, variableName));
		},
		get_joystick_angle: function (block, ctx, stream) {
			let id = XML.getId(block);
			let joystickName = asIdentifier(XML.getChildNode(block, "joystickName").innerText);

			ctx.addJoystickImport(joystickName);

			let selector = joystickName + "." + "getAngle";
			stream.push(builder.scriptCall(id, selector, []));
		},
		get_joystick_magnitude: function (block, ctx, stream) {
			let id = XML.getId(block);
			let joystickName = asIdentifier(XML.getChildNode(block, "joystickName").innerText);

			ctx.addJoystickImport(joystickName);

			let selector = joystickName + "." + "getMagnitude";
			stream.push(builder.scriptCall(id, selector, []));
		},
		declare_local_variable: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "variableName").innerText);
			let value = generateCodeForValue(block, ctx, "value");

			stream.push(builder.variableDeclaration(id, name, value));
		},
		list_get: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);
			let index = generateCodeForValue(block, ctx, "index");

			ctx.addListImport(listName);

			let selector = listName + "." + "get";
			let arg = {name: "index", value: index};
			stream.push(builder.scriptCall(id, selector, [arg]));
		},
		list_set: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "set";
			let args = [
				{name: "index", value: generateCodeForValue(block, ctx, "index")},
				{name: "value", value: generateCodeForValue(block, ctx, "value")},
			];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_push: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "push";
			let args = [
				{name: "value", value: generateCodeForValue(block, ctx, "value")},
			];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_pop: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "pop";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_clear: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "clear";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_count: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "count";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_size: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "size";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_random: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "get_random";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_sum: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "sum";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_avg: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "avg";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_max: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "max";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		list_min: function (block, ctx, stream) {
			let id = XML.getId(block);
			let listName = asIdentifier(XML.getChildNode(block, "listName").innerText);

			ctx.addListImport(listName);

			let selector = listName + "." + "min";
			let args = [];
			stream.push(builder.scriptCall(id, selector, args));
		},
		proc_definition_0args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [];
			stream.push(builder.procedure(id, name, args, statements));
		},
		proc_definition_1args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [asIdentifier(XML.getChildNode(block, "arg0").innerText)];
			stream.push(builder.procedure(id, name, args, statements));
		},
		proc_definition_2args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [asIdentifier(XML.getChildNode(block, "arg0").innerText),
									asIdentifier(XML.getChildNode(block, "arg1").innerText)];
			stream.push(builder.procedure(id, name, args, statements));
		},
		proc_definition_3args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [asIdentifier(XML.getChildNode(block, "arg0").innerText),
									asIdentifier(XML.getChildNode(block, "arg1").innerText),
									asIdentifier(XML.getChildNode(block, "arg2").innerText)];
			stream.push(builder.procedure(id, name, args, statements));
		},
		proc_call_0args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			stream.push(builder.scriptCall(id, scriptName, []));
		},
		proc_call_1args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let args = [{name: null, value: generateCodeForValue(block, ctx, "arg0")}];
			stream.push(builder.scriptCall(id, scriptName, args));
		},
		proc_call_2args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let args = [{name: null, value: generateCodeForValue(block, ctx, "arg0")},
									{name: null, value: generateCodeForValue(block, ctx, "arg1")}];
			stream.push(builder.scriptCall(id, scriptName, args));
		},
		proc_call_3args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let args = [{name: null, value: generateCodeForValue(block, ctx, "arg0")},
									{name: null, value: generateCodeForValue(block, ctx, "arg1")},
									{name: null, value: generateCodeForValue(block, ctx, "arg2")}];
			stream.push(builder.scriptCall(id, scriptName, args));
		},
		func_definition_0args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [];
			stream.push(builder.function(id, name, args, statements));
		},
		func_definition_1args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [asIdentifier(XML.getChildNode(block, "arg0").innerText)];
			stream.push(builder.function(id, name, args, statements));
		},
		func_definition_2args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [asIdentifier(XML.getChildNode(block, "arg0").innerText),
									asIdentifier(XML.getChildNode(block, "arg1").innerText)];
			stream.push(builder.function(id, name, args, statements));
		},
		func_definition_3args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let name = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let statements = [];
			generateCodeForStatements(block, ctx, "statements", statements);
			let args = [asIdentifier(XML.getChildNode(block, "arg0").innerText),
									asIdentifier(XML.getChildNode(block, "arg1").innerText),
									asIdentifier(XML.getChildNode(block, "arg2").innerText)];
			stream.push(builder.function(id, name, args, statements));
		},
		func_call_0args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			stream.push(builder.scriptCall(id, scriptName, []));
		},
		func_call_1args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let args = [{name: null, value: generateCodeForValue(block, ctx, "arg0")}];
			stream.push(builder.scriptCall(id, scriptName, args));
		},
		func_call_2args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let args = [{name: null, value: generateCodeForValue(block, ctx, "arg0")},
									{name: null, value: generateCodeForValue(block, ctx, "arg1")}];
			stream.push(builder.scriptCall(id, scriptName, args));
		},
		func_call_3args: function (block, ctx, stream) {
			let id = XML.getId(block);
			let scriptName = asIdentifier(XML.getChildNode(block, "scriptName").innerText);
			let args = [{name: null, value: generateCodeForValue(block, ctx, "arg0")},
									{name: null, value: generateCodeForValue(block, ctx, "arg1")},
									{name: null, value: generateCodeForValue(block, ctx, "arg2")}];
			stream.push(builder.scriptCall(id, scriptName, args));
		},
		return: function (block, ctx, stream) {
			let id = XML.getId(block);
			stream.push(builder.return(id, null));
		},
		return_value: function (block, ctx, stream) {
			let id = XML.getId(block);
			stream.push(builder.return(id, generateCodeForValue(block, ctx, "value")));
		},
		start_tone: function (block, ctx, stream) {
			let id = XML.getId(block);
			let selector = "startTone";
			let args = [
				generateCodeForValue(block, ctx, "pinNumber"),
				generateCodeForValue(block, ctx, "tone"),
			];
			stream.push(builder.primitiveCall(id, selector, args));
		},
		play_tone: function (block, ctx, stream) {
			let id = XML.getId(block);
			let selector = "playTone";
			let tone = generateCodeForValue(block, ctx, "tone");
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let time = generateCodeForValue(block, ctx, "time")
			let unit = XML.getChildNode(block, "unit").innerText;
			let delay;
			if (unit === "ms") {
				delay = time;
			}	else if (unit === "s") {
				delay = builder.primitiveCall(id, "*", [time, builder.number(id, 1000)]);
			}	else if (unit === "m") {
				delay = builder.primitiveCall(id, "*", [time, builder.number(id, 60000)]);
			}	else {
				throw "Invalid delay unit: '" + unit + "'";
			}
			stream.push(builder.primitiveCall(id, selector, [pinNumber, tone, delay]));
		},
		start_note: function (block, ctx, stream) {
			let id = XML.getId(block);
			let selector = "startTone";
			let note = parseInt(XML.getChildNode(block, "note").innerText);
			let args = [
				generateCodeForValue(block, ctx, "pinNumber"),
				builder.number(id, note)
			];
			stream.push(builder.primitiveCall(id, selector, args));
		},
		play_note: function (block, ctx, stream) {
			let id = XML.getId(block);
			let selector = "playTone";
			let note = builder.number(id, parseInt(XML.getChildNode(block, "note").innerText));
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let time = generateCodeForValue(block, ctx, "time")
			let unit = XML.getChildNode(block, "unit").innerText;
			let delay;
			if (unit === "ms") {
				delay = time;
			}	else if (unit === "s") {
				delay = builder.primitiveCall(id, "*", [time, builder.number(id, 1000)]);
			}	else if (unit === "m") {
				delay = builder.primitiveCall(id, "*", [time, builder.number(id, 60000)]);
			}	else {
				throw "Invalid delay unit: '" + unit + "'";
			}
			stream.push(builder.primitiveCall(id, selector, [pinNumber, note, delay]));
		},
		stop_tone: function (block, ctx, stream) {
			let id = XML.getId(block);
			let selector = "stopTone";
			let args = [
				generateCodeForValue(block, ctx, "pinNumber"),
			];
			stream.push(builder.primitiveCall(id, selector, args));
		},
		stop_tone_wait: function (block, ctx, stream) {
			let id = XML.getId(block);
			let selector = "stopToneAndWait";
			let pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			let time = generateCodeForValue(block, ctx, "time")
			let unit = XML.getChildNode(block, "unit").innerText;
			let delay;
			if (unit === "ms") {
				delay = time;
			}	else if (unit === "s") {
				delay = builder.primitiveCall(id, "*", [time, builder.number(id, 1000)]);
			}	else if (unit === "m") {
				delay = builder.primitiveCall(id, "*", [time, builder.number(id, 60000)]);
			}	else {
				throw "Invalid delay unit: '" + unit + "'";
			}
			stream.push(builder.primitiveCall(id, selector, [pinNumber, delay]));
		},
		button_wait_for_action: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pin = generateCodeForValue(block, ctx, "pinNumber");
			let action = XML.getChildNode(block, "action").innerText;
			let selector;
			if (action === "press") {	selector = "waitForPress"; }
			else if (action === "release") { selector = "waitForRelease"}
			else {
				throw "Invalid button action: '" + action + "'";
			}
			selector = "buttons." + selector;
			ctx.addButtonsImport();
			stream.push(builder.primitiveCall(id, selector, [pin]));
		},
		button_check_state: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pin = generateCodeForValue(block, ctx, "pinNumber");
			let state = XML.getChildNode(block, "state").innerText;
			let selector;
			if (state === "press") {	selector = "isPressed"; }
			else if (state === "release") { selector = "isReleased"}
			else {
				throw "Invalid button state: '" + state + "'";
			}
			selector = "buttons." + selector;
			ctx.addButtonsImport();
			stream.push(builder.primitiveCall(id, selector, [pin]));
		},
		button_ms_holding: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pin = generateCodeForValue(block, ctx, "pinNumber");
			let selector = "buttons.millisecondsHolding";
			ctx.addButtonsImport();
			stream.push(builder.primitiveCall(id, selector, [pin]));
		},
		button_wait_for_long_action: function (block, ctx, stream) {
			let id = XML.getId(block);
			let pin = generateCodeForValue(block, ctx, "pinNumber");
			let time = generateCodeForValue(block, ctx, "time");

			let unit = XML.getChildNode(block, "unit").innerText;
			let duration;
			if (unit === "ms") {
				duration = time;
			}	else if (unit === "s") {
				duration = builder.primitiveCall(id, "*", [time, builder.number(id, 1000)]);
			}	else if (unit === "m") {
				duration = builder.primitiveCall(id, "*", [time, builder.number(id, 60000)]);
			}	else {
				throw "Invalid time unit: '" + unit + "'";
			}

			let action = XML.getChildNode(block, "action").innerText;
			let selector;
			if (action === "press") {	selector = "waitForHold"; }
			else if (action === "release") { selector = "waitForHoldAndRelease"}
			else {
				throw "Invalid button action: '" + action + "'";
			}
			selector = "buttons." + selector;
			ctx.addButtonsImport();
			stream.push(builder.primitiveCall(id, selector, [pin, duration]));
		},
		pin_cast: function (block, ctx, stream) {
			let child = XML.getChildNode(block, "value");
			if (child === undefined) return undefined;
			generateCodeFor(XML.getLastChild(child), ctx, stream);
		},
		number_cast: function (block, ctx, stream) {
			let child = XML.getChildNode(block, "value");
			if (child === undefined) return undefined;
			generateCodeFor(XML.getLastChild(child), ctx, stream);
		},
		boolean_cast: function (block, ctx, stream) {
			let child = XML.getChildNode(block, "value");
			if (child === undefined) return undefined;
			generateCodeFor(XML.getLastChild(child), ctx, stream);
		},
	};

	function asIdentifier(str) {
		return str.replace(/ /g, '_');
	}

	function generateCodeFor(block, ctx, stream) {
		if (isDisabled(block)) return undefined;

		let type = block.getAttribute("type");
		let func = dispatchTable[type];
		if (func == undefined) {
			throw "CODEGEN ERROR: Type not found '" + type + "'";
		}
		try {
			ctx.path.push(block);
			func(block, ctx, stream);
		}
		finally {
			ctx.path.pop();
		}
	}

	function generateCodeForValue(block, ctx, name) {
		let child = XML.getChildNode(block, name);
		if (child === undefined) return undefined;
		let stream = [];
		generateCodeFor(XML.getLastChild(child), ctx, stream);
		if (stream.length != 1) {
			throw "CODEGEN ERROR: Value block didn't generate single code element";
		}
		return stream[0];
	}

	function generateCodeForStatements(block, ctx, name, stream) {
		let child = XML.getChildNode(block, name || "statements");
		if (child !== undefined) {
			child.childNodes.forEach(function (each) {
				let next = each;
				do {
					try {
						let temp = [];
						generateCodeFor(next, ctx, temp);
						stream.push.apply(stream, temp);
					} catch (err) {
						console.log(err);
					}
					next = getNextStatement(next);
				} while (next !== undefined);
			});
		}
	}

	function getNextStatement(block) {
		let next = XML.getLastChild(block, child => child.tagName === "NEXT");
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
		generate: function (xml, metadata) {
			let setup = [];
			let scripts = [];
			let ctx = {
				path: [xml],
				imports: new Map(),
				globals: [],

				/*
				 * NOTE(Richo): For now, the only blocks capable of declaring local variables
				 * are "declare_local_variable", "for", and the procedure definition blocks.
				 * Unfortunately, "declare_local_variable" works a little different than the
				 * rest so we need special code to traverse the xml tree.
				 */
				isLocalDefined: function (name) {
					/*
					 * For the "declare_local_variable" block we walk from the current block
					 * element up through its parent chain looking for this type of block
					 * and we check if it declares a variable with the specified name. If we
					 * find it then we don't have to keep looking, we just return true.
					 */
					{
						let currentElement = ctx.path[ctx.path.length - 1];
						while (currentElement != null) {
							if (currentElement.getAttribute("type") == "declare_local_variable"
									&& !isDisabled(currentElement)) {
								let field = XML.getChildNode(currentElement, "variableName");
								if (field != undefined && field.innerText == name) {
									return true; // We found our variable declaration!
								}
							}
							currentElement = currentElement.parentElement;
						}
					}

					/*
					 * In the other cases, we just need to look at the ctx.path to find
					 * the desired block. So, we start by filtering the path and then we
					 * check if any of the blocks found define a variable with the specified
					 * name.
					 */
					{
						let interestingBlocks = {
							for: ["variableName"],
							proc_definition_1args: ["arg0"],
							proc_definition_2args: ["arg0", "arg1"],
							proc_definition_3args: ["arg0", "arg1", "arg2"],
							func_definition_1args: ["arg0"],
							func_definition_2args: ["arg0", "arg1"],
							func_definition_3args: ["arg0", "arg1", "arg2"]
						};
						let interestingTypes = new Set(Object.keys(interestingBlocks));
						let blocks = ctx.path.filter(b => interestingTypes.has(b.getAttribute("type")));
						if (blocks.some(function (b) {
							let fields = interestingBlocks[b.getAttribute("type")];
							return fields.some(function (f) {
								let field = XML.getChildNode(b, f);
								return field != undefined && field.innerText == name;
							});
						})) {
							return true; // We found our variable declaration!
						}
					}

					// If we got here, the variable is not declared yet...
					return false;
				},

				addDCMotorImport: function (alias) {
					ctx.addImport(alias, "DCMotor.uzi", function () {
						let motor = metadata.motors.find(function (m) { return m.name === alias; });
						if (motor == undefined) return null;

						function pin(pin) {
							let type = pin[0];
							let number = parseInt(pin.slice(1));
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
						let sonar = metadata.sonars.find(function (m) { return m.name === alias; });
						if (sonar == undefined) return null;

						function pin(pin) {
							let type = pin[0];
							let number = parseInt(pin.slice(1));
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
				addJoystickImport: function (alias) {
					ctx.addImport(alias, "Joystick.uzi", function () {
						let joystick = metadata.joysticks.find(function (m) { return m.name === alias; });
						if (joystick == undefined) return null;

						function pin(pin) {
							let type = pin[0];
							let number = parseInt(pin.slice(1));
							return builder.pin(null, type, number);
						}

						let stmts = [];
						stmts.push(builder.assignment(null, "xPin", pin(joystick.xPin)));
						stmts.push(builder.assignment(null, "yPin", pin(joystick.yPin)));
						stmts.push(builder.start(null, ["reading"]));
						return builder.block(null, stmts);
					});
				},
				addButtonsImport: function () {
					ctx.addImport("buttons", "Buttons.uzi", function () {
						let stmts = [];
						stmts.push(builder.assignment(null, "debounceMs", builder.number(null, 50)));
						return builder.block(null, stmts);
					});
				},
				addListImport: function (alias) {
					ctx.addImport(alias, "List.uzi", function () {
						let list = metadata.lists.find(function (m) { return m.name === alias; });
						if (list == undefined) return null;

						let stmts = [];
						stmts.push(builder.assignment(null, "size", builder.number(null, parseInt(list.size))));
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
			Array.from(xml.childNodes).filter(isTopLevel).forEach(function (block) {
				try {
					generateCodeFor(block, ctx, scripts);
				} catch (err) {
					console.log(err);
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
				Array.from(ctx.imports, entry => entry[1]),
				ctx.globals.map(varName => metadata.variables.find(v => v.name == varName)),
				scripts);
		}
	}
})();
