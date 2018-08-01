var CodeGenerator = (function () {
	
	var topLevelBlocks = ["task", "timer"];	
	var dispatchTable =  {
		task: function (block, path) {
			var id = getId(block);
			var taskName = getChildNode(block, "taskName").innerText;
			var statements = generateCodeForStatements(block, path);
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
		forever: function (block, path) {
			var id = getId(block);
			var statements = generateCodeForStatements(block, path);
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
		for: function (block, path) {			
			var id = getId(block);
			var variableName = getChildNode(block, "variable").innerText;
			var start = generateCodeForValue(block, path, "start");
			var stop = generateCodeForValue(block, path, "stop");
			var step = generateCodeForValue(block, path, "step");
			var statements = generateCodeForStatements(block, path);
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
		math_number: function (block, path) {
			var id = getId(block);
			var value = parseFloat(getChildNode(block, "NUM").innerText);
			return {
				type: "UziNumberNode",
				id: id,
				value: value
			}
		},
		turn_pin_variable: function (block, path) {
			var id = getId(block);
			var pinState = getChildNode(block, "pinState").innerText;
			var pinNumber = generateCodeForValue(block, path, "pinNumber");
			var selector = pinState === "on" ? "turnOn" : "turnOff";
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: selector,
				arguments: [pinNumber],
				primitiveName: selector
			}
		},
		variables_get: function (block, path) {
			var id = getId(block);
			var variableName = getChildNode(block, "VAR").innerText;
			return {
				type: "UziVariableNode",
				id: id,
				name: variableName
			}
		},
		delay: function (block, path) {
			var id = getId(block);
			var unit = getChildNode(block, "unit").innerText;
			var time = generateCodeForValue(block, path, "time");
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
		start_task: function (block, path) {
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
		stop_task: function (block, path) {
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
		resume_task: function (block, path) {
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
		pause_task: function (block, path) {
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
		run_task: function (block, path) {
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
		conditional_simple: function (block, path) {
			var id = getId(block);
			var condition = generateCodeForValue(block, path, "condition");
			var trueBranch = generateCodeForStatements(block, path, "trueBranch");
			return {
				type: "UziConditionalNode",
				id: id,
				condition: condition,
				trueBranch: {
					type: "UziBlockNode",
					id: id,
					statements: trueBranch
				}
			}
		},
		conditional_full: function (block, path) {
			var id = getId(block);
			var condition = generateCodeForValue(block, path, "condition");
			var trueBranch = generateCodeForStatements(block, path, "trueBranch");
			var falseBranch = generateCodeForStatements(block, path, "falseBranch");
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
		logic_compare: function (block, path) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, path, "A");
			var right = generateCodeForValue(block, path, "B");
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
		elapsed_time: function (block, path) {
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
		toggle_variable: function (block, path) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, path, "pinNumber");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "toggle",
				arguments: [pinNumber],
				primitiveName: "toggle"
			};
		},
		logic_operation: function (block, path) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, path, "A");
			var right = generateCodeForValue(block, path, "B");
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
		logic_boolean: function (block, path) {
			var id = getId(block);
			var bool = getChildNode(block, "BOOL").innerText;			
			return {
				type: "UziNumberNode",
				id: id,
				value: bool === "TRUE" ? 1 : 0
			};
		},
		logic_negate: function (block, path) {
			var id = getId(block);
			var bool = generateCodeForValue(block, path, "BOOL");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "not",
				arguments: [bool],
				primitiveName: "negate"
			};
		},
		math_number_property: function (block, path) {
			var id = getId(block);
			var type = getChildNode(block, "PROPERTY").innerText;
			var num = generateCodeForValue(block, path, "NUMBER_TO_CHECK");
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
			} else {
				throw "Math number property not found: '" + type + "'";
			}
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "not",
				arguments: [num],
				primitiveName: "negate"
			};
		},
		repeat_times: function (block, path) {
			var id = getId(block);
			var times = generateCodeForValue(block, path, "times");
			var statements = generateCodeForStatements(block, path);
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
		math_round: function (block, path) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var num = generateCodeForValue(block, path, "NUM");
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
		math_single: function (block, path) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var num = generateCodeForValue(block, path, "NUM");
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
		math_trig: function (block, path) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var num = generateCodeForValue(block, path, "NUM");
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
		math_constant: function (block, path) {
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
				value = Infinity;
			} else {
				throw "Math constant not found: '" + type + "'";
			}
			return {
				type: "UziNumberNode",
				id: id,
				value: value
			};
		},
		math_arithmetic: function (block, path) {
			var id = getId(block);
			var type = getChildNode(block, "OP").innerText;
			var left = generateCodeForValue(block, path, "A");
			var right = generateCodeForValue(block, path, "B");
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
		timer: function (block, path) {
			var id = getId(block);
			var name = getChildNode(block, "taskName").innerText;
			var runningTimes = parseInt(getChildNode(block, "runningTimes").innerText);
			var tickingScale = getChildNode(block, "tickingScale").innerText;
			var initialState = getChildNode(block, "initialState").innerText;
			var statements = generateCodeForStatements(block, path);			
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
		write_pin_variable: function (block, path) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, path, "pinNumber");
			var pinValue = generateCodeForValue(block, path, "pinValue");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "write",
				arguments: [pinNumber, pinValue],
				primitiveName: "write"
			}
		},
		read_pin_variable: function (block, path) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, path, "pinNumber");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "read",
				arguments: [pinNumber],
				primitiveName: "read"
			};
		},
		degrees_servo_variable: function (block, path) {
			var id = getId(block);
			var pinNumber = generateCodeForValue(block, path, "pinNumber");
			var servoValue = generateCodeForValue(block, path, "servoValue");
			return {
				type: "UziPrimitiveCallNode",
				id: id,
				selector: "servoDegrees",
				arguments: [pinNumber, servoValue],
				primitiveName: "servoDegrees"
			};
		},
		repeat: function (block, path) {
			var id = getId(block);
			var negated = getChildNode(block, "negate").innerText === "true";
			var condition = generateCodeForValue(block, path, "condition");
			var statements = generateCodeForStatements(block, path);
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
		}
	};
	
	function generateCodeFor(block, path) {
		var type = block.getAttribute("type");
		var func = dispatchTable[type];
		if (func == undefined) {
			throw "CODEGEN ERROR: Type not found '" + type + "'";
		}
		try {
			path.push(block);
			return func(block, path);
		}
		finally {
			path.pop();
		}
	}
	
	function generateCodeForValue(block, path, name) {
		return generateCodeFor(getLastChild(getChildNode(block, name)), path);
	}
	
	function generateCodeForStatements(block, path, name) {
		var statements = [];
		var child = getChildNode(block, name || "statements");
		if (child !== undefined) {
			child.childNodes.forEach(function (each) {
				var next = each;
				do {
					statements.push(generateCodeFor(next, path));
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
			xml.childNodes.forEach(function (block) {
				if (isTopLevel(block)) {
					var path = [xml];
					var code = generateCodeFor(block, path);
					scripts.push(code);
				}
			});
			return {
				type: "UziProgramNode",
				imports: [],
				globals: [],
				scripts: scripts
			};
		}
	}
})();