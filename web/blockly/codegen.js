var CodeGenerator = (function () {
	
	var topLevelBlocks = ["setup_task", "task"];	
	var dispatchTable =  {
		setup_task : function (block, path) {
			var id = getId(block);
			var scriptName = getChildNode(block, "scriptName").innerText;
			var statements = generateCodeForStatements(block, path);
			return {
				type: "UziTaskNode",
				id: id,
				name: scriptName,
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
	
	function generateCodeForStatements(block, path) {
		var statements = [];
		getChildNode(block, "statements").childNodes.forEach(function (each) {
			var next = each;
			do {
				statements.push(generateCodeFor(next, path));
				next = getNextStatement(next);
			} while (next !== undefined);
		});
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