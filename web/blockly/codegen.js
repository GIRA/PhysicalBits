var CodeGenerator = (function () {
	
	var topLevelBlocks = ["task", "timer", "procedures_defnoreturn", "procedures_defreturn"];	
	var dispatchTable =  {
		task: function (block, ctx) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			var statements = generateCodeForStatements(block, ctx);
			return {
				type: "UziTaskNode",
				id: id,
				name: taskName,
				arguments: [],
				state: "once",
				tickingRate: null,
				body: {
					type: "UziBlockNode",
					id: id,
					statements: statements					
				}
			};
		},
		forever: function (block, ctx) {
			var id = getId(block);
			var statements = generateCodeForStatements(block, ctx);
			return {
				type: "UziForeverNode",
				id: id,
				body: {
					type: "UziBlockNode",
					id: id,
					statements: statements					
				}
			};
		},
		for: function (block, ctx) {			
			var id = getId(block);
			var variableName = getChildNode(block, "variable").innerText;
			var start = generateCodeForValue(block, ctx, "start");
			var stop = generateCodeForValue(block, ctx, "stop");
			var step = generateCodeForValue(block, ctx, "step");
			var statements = generateCodeForStatements(block, ctx);
			return {
				type: "UziForNode",
				id: id,
				counter: {
					type: "UziVariableDeclarationNode",
					id: id,
					name: variableName,
					value: null
				},
				start: start,
				stop: stop,
				step: step,
				body: {
					type: "UziBlockNode",
					id: id,
					statements: statements					
				}
			}
		},
		math_number: function (block, ctx) {
			var id = getId(block);
			var value = parseFloat(getChildNode(block, "NUM").innerText);
			return {
				type: "UziNumberNode",
				id: id,
				value: value
			}
		},
		turn_pin_variable: function (block, ctx) {
			var id = getId(block);
			var pinState = getChildNode(block, "pinState").innerText;
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var selector = pinState === "on" ? "turnOn" : "turnOff";
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [pinNumber],
				primitiveName: selector
			}
		},
		variables_get: function (block, ctx) {
			var id = getId(block);
			var variableName = getChildNode(block, "VAR").innerText;
			ctx.addGlobal(variableName);
			return {
				type: "UziVariableNode",
				id: id,
				name: variableName
			}
		},
		delay: function (block, ctx) {
			var id = getId(block);
			var unit = getChildNode(block, "unit").innerText;
			var time = generateCodeForValue(block, ctx, "time");
			var selector;
			if (unit === "ms") { selector = "delayMs"; }
			else if (unit === "s") { selector = "delayS"; }
			else if (unit === "m") { selector = "delayM"; }
			else {
				throw "Invalid delay unit: '" + unit + "'";
			}
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [time],
				primitiveName: selector
			}
		},
		start_task: function (block, ctx) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			return {
				type: "UziScriptStartNode",
				id: id,
				scripts: [{
					type: "UziScriptRefNode",
					id: id,
					scriptName: taskName
				}]
			}
		},
		stop_task: function (block, ctx) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			return {
				type: "UziScriptStopNode",
				id: id,
				scripts: [{
					type: "UziScriptRefNode",
					id: id,
					scriptName: taskName
				}]
			}
		},
		resume_task: function (block, ctx) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			return {
				type: "UziScriptResumeNode",
				id: id,
				scripts: [{
					type: "UziScriptRefNode",
					id: id,
					scriptName: taskName
				}]
			}
		},
		pause_task: function (block, ctx) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			return {
				type: "UziScriptPauseNode",
				id: id,
				scripts: [{
					type: "UziScriptRefNode",
					id: id,
					scriptName: taskName
				}]
			}
		},
		run_task: function (block, ctx) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			return {
				type: "UziScriptCallNode",
				id: id,
				script: {
					type: "UziScriptRefNode",
					id: id,
					scriptName: taskName
				},
				arguments: []
			}
		},
		conditional_simple: function (block, ctx) {
			var id = getId(block);
			var condition = generateCodeForValue(block, ctx, "condition");
			var trueBranch = generateCodeForStatements(block, ctx, "trueBranch");
			return {
				type: "UziConditionalNode",
				id: id,
				condition: condition,
				trueBranch: {
					type: "UziBlockNode",
					id: id,
					statements: trueBranch
				},
				falseBranch: {
					type: "UziBlockNode",
					id: id,
					statements: []
				}
			}
		},
		conditional_full: function (block, ctx) {
			var id = getId(block);
			var condition = generateCodeForValue(block, ctx, "condition");
			var trueBranch = generateCodeForStatements(block, ctx, "trueBranch");
			var falseBranch = generateCodeForStatements(block, ctx, "falseBranch");
			return {
				type: "UziConditionalNode",
				id: id,
				condition: condition,
				trueBranch: {
					type: "UziBlockNode",
					id: id,
					statements: trueBranch
				},
				falseBranch: {
					type: "UziBlockNode",
					id: id,
					statements: falseBranch
				}
			}
		},
		logic_compare: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, ctx, "A");
			var right = generateCodeForValue(block, ctx, "B");
			var selector, primName;
			if (type === "EQ") {
				selector = "=";
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
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [left, right],
				primitiveName: primName
			}
		},
		elapsed_time: function (block, ctx) {
			var id = getId(block);
			var unit = getChildNode(block, "unit").innerText;
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
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [],
				primitiveName: primName
			};
		},
		toggle_variable: function (block, ctx) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "toggle",
				arguments: [pinNumber],
				primitiveName: "toggle"
			};
		},
		logic_operation: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, ctx, "A");
			var right = generateCodeForValue(block, ctx, "B");
			var selector, primName;
			if (type === "AND") {
				selector = "&&";
				primName = "logicalAnd";
			} else if (type === "OR") {
				selector = "||";
				primName = "logicalOr";
			}
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [left, right],
				primitiveName: primName
			};
		},
		logic_boolean: function (block, ctx) {
			var id = getId(block);
			var bool = getChildNode(block, "BOOL").innerText;			
			return {
				type: "UziNumberNode",
				id: id,
				value: bool === "TRUE" ? 1 : 0
			};
		},
		logic_negate: function (block, ctx) {
			var id = getId(block);
			var bool = generateCodeForValue(block, ctx, "BOOL");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "not",
				arguments: [bool],
				primitiveName: "negate"
			};
		},
		math_number_property: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "PROPERTY").innerText;
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
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: args,
				primitiveName: primName
			};
		},
		repeat_times: function (block, ctx) {
			var id = getId(block);
			var times = generateCodeForValue(block, ctx, "times");
			var statements = generateCodeForStatements(block, ctx);
			return {
				type: "UziRepeatNode",
				id: id,
				times: times,
				body: {
					type: "UziBlockNode",
					id: id,
					statements: statements
				}
			};
		},
		math_round: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
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
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [num],
				primitiveName: primName
			};
		},
		math_single: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var num = generateCodeForValue(block, ctx, "NUM");
			var selector, primName;
			if (type === "ROOT") {
				selector = "sqrt";
				primName = "sqrt";
			} else if (type === "ABS") {
				selector = "abs";
				primName = "abs";
			} else if (type === "NEG") {
				selector = "negate";
				primName = "negate";
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
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [num],
				primitiveName: primName
			};
		},
		math_trig: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
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
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [num],
				primitiveName: primName
			};
		},
		math_constant: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "CONSTANT").innerText;
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
			return {
				type: "UziNumberNode",
				id: id,
				value: value
			};
		},
		math_arithmetic: function (block, ctx) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
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
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [left, right],
				primitiveName: primName
			};
		},
		timer: function (block, ctx) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			var runningTimes = parseInt(getChildNode(block, "runningTimes").innerText);
			var tickingScale = getChildNode(block, "tickingScale").innerText;
			var initialState = getChildNode(block, "initialState").innerText;
			var statements = generateCodeForStatements(block, ctx);			
			return {
				type: "UziTaskNode",
				id: id,
				name: taskName,
				arguments: [],
				state: initialState === "started" ? "running" : "stopped",
				tickingRate: {
					type: "UziTickingRateNode",
					id: id,
					value: runningTimes,
					scale: tickingScale
				},
				body: {
					type: "UziBlockNode",
					id: id,
					statements: statements					
				}
			};
		},
		write_pin_variable: function (block, ctx) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var pinValue = generateCodeForValue(block, ctx, "pinValue");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "write",
				arguments: [pinNumber, pinValue],
				primitiveName: "write"
			}
		},
		read_pin_variable: function (block, ctx) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "read",
				arguments: [pinNumber],
				primitiveName: "read"
			};
		},
		degrees_servo_variable: function (block, ctx) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var servoValue = generateCodeForValue(block, ctx, "servoValue");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "servoDegrees",
				arguments: [pinNumber, servoValue],
				primitiveName: "servoDegrees"
			};
		},
		repeat: function (block, ctx) {
			var id = getId(block);
			var negated = getChildNode(block, "negate").innerText === "true";
			var condition = generateCodeForValue(block, ctx, "condition");
			var statements = generateCodeForStatements(block, ctx);
			return {
				type: "UziWhileNode",
				id: id,
				pre: {
					type: "UziBlockNode",
					id: id,
					statements: []
				},
				condition: condition,
				post: {
					type: "UziBlockNode",
					id: id,
					statements: statements
				},
				negated: negated
			};
		},
		is_pin_variable: function (block, ctx) {
			var id = getId(block);
			var pinState = getChildNode(block, "pinState").innerText;
			var pinNumber = generateCodeForValue(block, ctx, "pinNumber");
			var selector, primName;
			primName = selector = pinState === "on" ? "isOn" : "isOff";			
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [pinNumber],
				primitiveName: primName
			};
		},
		wait: function (block, ctx) {
			var id = getId(block);
			var negated = getChildNode(block, "negate").innerText === "true";
			var condition = generateCodeForValue(block, ctx, "condition");
			return {
				type: "UziWhileNode",
				id: id,
				pre: {
					type: "UziBlockNode",
					id: id,
					statements: []
				},
				condition: condition,
				post: {
					type: "UziBlockNode",
					id: id,
					statements: []
				},
				negated: negated
			};
		},
		math_modulo: function (block, ctx) {
			var id = getId(block);
			var left = generateCodeForValue(block, ctx, "DIVIDEND");
			var right = generateCodeForValue(block, ctx, "DIVISOR");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "%",
				arguments: [left, right],
				primitiveName: "mod"
			};
		},
		variables_set: function (block, ctx) {
			var id = getId(block);
			var name = getChildNode(block, "VAR").innerText;
			ctx.addGlobal(name);
			var value = generateCodeForValue(block, ctx, "VALUE");
			return {
				type: "UziAssignmentNode",
				id: id,
				left: {
					type: "UziVariableNode",
					id: id,
					name: name
				},
				right: value
			};
		},
		math_change: function (block, ctx) {
			var id = getId(block);
			var name = getChildNode(block, "VAR").innerText;
			var delta = generateCodeForValue(block, ctx, "DELTA");
			var variable = {
				type: "UziVariableNode",
				id: id,
				name: name
			};
			return {
				type: "UziAssignmentNode",
				id: id,
				left: variable,
				right: {
					type: "UziPrimitiveCallNode",
					id: id,
					selector: "+",
					arguments: [variable, delta],
					primitiveName: "add"
				}
			};
		},
		math_constrain: function (block, ctx) {
			var id = getId(block);
			var value = generateCodeForValue(block, ctx, "VALUE");
			var low = generateCodeForValue(block, ctx, "LOW");
			var high = generateCodeForValue(block, ctx, "HIGH");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "constrain",
				arguments: [value, low, high],
				primitiveName: "constrain"
			};
		},
		math_random_int: function (block, ctx) {
			var id = getId(block);
			var from = generateCodeForValue(block, ctx, "FROM");
			var to = generateCodeForValue(block, ctx, "TO");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "randomInt",
				arguments: [from, to],
				primitiveName: "randomInt"
			};
		},
		math_random_float: function (block, ctx) {
			var id = getId(block);
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "random",
				arguments: [],
				primitiveName: "random"
			};
		},
		procedures_defnoreturn: function (block, ctx) {
			var id = getId(block);
			var name = getChildNode(block, "NAME").innerText;
			var mutation = getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var args = [];
			mutation.childNodes.forEach(function (each) {
				args.push(each.getAttribute("name"));
			});			
			var statements = generateCodeForStatements(block, ctx, "STACK");
			return {
				type: "UziProcedureNode",
				id: id,
				name: name,
				arguments: args.map(function (varName) {
					return {
						type: "UziVariableDeclarationNode",
						id: id,
						name: varName,
						value: null
					};
				}),
				body: {
					type: "UziBlockNode",
					id: id,
					statements: statements
				}
			};
		},
		comment_statement: function (block, ctx) {
			return undefined;
		},
		procedures_callnoreturn: function (block, ctx) {
			var id = getId(block);
			var mutation = getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var scriptName = mutation.getAttribute("name");
			var argNames = [];
			mutation.childNodes.forEach(function (each) {
				argNames.push(each.getAttribute("name"));
			});
			var args = [];
			for (var i = 0; i < argNames.length; i++) {
				var value = generateCodeForValue(block, ctx, "ARG" + i);
				var key = argNames[i];
				args.push({
					type: "Association",
					key: key,
					value: value
				});
			}
			return {
				type: "UziScriptCallNode",
				id: id,
				script:  {
					type: "UziScriptRefNode",
					id: id,
					scriptName: scriptName
				},
				arguments: args
			};
		},
		procedures_callreturn: function (block, ctx) {
			var id = getId(block);
			var mutation = getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var scriptName = mutation.getAttribute("name");
			var argNames = [];
			mutation.childNodes.forEach(function (each) {
				argNames.push(each.getAttribute("name"));
			});
			var args = [];
			for (var i = 0; i < argNames.length; i++) {
				var value = generateCodeForValue(block, ctx, "ARG" + i);
				var key = argNames[i];
				args.push({
					type: "Association",
					key: key,
					value: value
				});
			}
			return {
				type: "UziScriptCallNode",
				id: id,
				script:  {
					type: "UziScriptRefNode",
					id: id,
					scriptName: scriptName
				},
				arguments: args
			};
		},
		procedures_ifreturn: function (block, ctx) {
			var id = getId(block);
			var condition = generateCodeForValue(block, ctx, "CONDITION");
			var value = generateCodeForValue(block, ctx, "VALUE");
			return {
				type: "UziConditionalNode",
				id: id,
				condition: condition,
				trueBranch: {
					type: "UziBlockNode",
					id: id,
					statements: [{
						type: "UziReturnNode",
						id: id,
						value: value || null
					}]
				},
				falseBranch: {
					type: "UziBlockNode",
					id: id,
					statements: []
				}
			};
		},
		procedures_defreturn: function (block, ctx) {
			var id = getId(block);
			var name = getChildNode(block, "NAME").innerText;
			var mutation = getLastChild(block, function (child) {
				return child.tagName === "MUTATION";
			});
			var args = [];
			mutation.childNodes.forEach(function (each) {
				args.push(each.getAttribute("name"));
			});
			var statements = generateCodeForStatements(block, ctx, "STACK");
			// TODO(Richo): Decide what to do if the return block is not defined
			var returnExpr = generateCodeForValue(block, ctx, "RETURN");
			statements.push({
				type: "UziReturnNode",
				id: id,
				value: returnExpr
			});
			return {
				type: "UziFunctionNode",
				id: id,
				name: name,
				arguments: args.map(function (varName) {
					return {
						type: "UziVariableDeclarationNode",
						id: id,
						name: varName,
						value: null
					};
				}),
				body: {
					type: "UziBlockNode",
					id: id,
					statements: statements
				}
			};
		},
		comment_expression: function (block, ctx) {
			return generateCodeForValue(block, ctx, "NAME");
		},
	};
	
	function generateCodeFor(block, ctx) {
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
		var child = getChildNode(block, name);
		if (child === undefined) return undefined;
		return generateCodeFor(getLastChild(child), ctx);
	}
	
	function generateCodeForStatements(block, ctx, name) {
		var statements = [];
		var child = getChildNode(block, name || "statements");
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
	
	function getId(block) {
		return block.getAttribute("id");
	}
	
	function getNextStatement(block) {
		var next = getLastChild(block, function (child) {
			return child.tagName === "NEXT";
		});
		if (next === undefined) { return next; }
		return next.childNodes[0];
	}
	
	function getChildNode(block, name) {
		return getLastChild(block, function (child) {
			return child.getAttribute("name") === name;
		});
	}
	
	function getLastChild(block, predicate) {
		for (var i = block.childNodes.length - 1; i >= 0; i--) {
			var child = block.childNodes[i];
			if (predicate === undefined || predicate(child)) {
				return child;
			}
		}
		return undefined;
	}
	
	function isTopLevel(block) {
		return topLevelBlocks.indexOf(block.getAttribute("type")) != -1;
	}
	
	return {
		generate: function (xml) {
			var scripts = [];
			var ctx = {
				path: [xml],
				globals: [],
				
				addGlobal: function (varName) {
					if (!ctx.globals.includes(varName)) {
						ctx.globals.push(varName);
					}
				}
			};
			xml.childNodes.forEach(function (block) {
				if (isTopLevel(block)) {
					var code = generateCodeFor(block, ctx);
					scripts.push(code);
				}
			});
			return {
				type: "UziProgramNode",
				imports: [],
				globals: ctx.globals.map(function (varName) {
					return {
						type: "UziVariableDeclarationNode",
						name: varName,
						value: null
					};
				}),
				scripts: scripts
			};
		}
	}
})();