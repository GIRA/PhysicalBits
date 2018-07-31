var CodeGenerator = (function () {
	
	var topLevelBlocks = ["setup_task", "task"];	
	var dispatchTable =  {
		setup_task : function (block, path) {
			return path;
		}
	};
	
	function generateCodeFor(block, path) {
		var type = block.getAttribute("type");
		var func = dispatchTable[type];
		if (func == undefined) {
			throw "CODEGEN ERROR: Type not found " + type;
		}
		try {
			path.push(block);
			return func(block, path);
		}
		finally {
			path.pop();
		}
	}
	
	function isTopLevel(block) {
		return topLevelBlocks.indexOf(block.getAttribute("type")) != -1;
	}
	
	return {
		generate: function (xml) {
			var tasks = [];
			xml.childNodes.forEach(function (block) {
				if (isTopLevel(block)) {
					var path = [xml];
					var code = generateCodeFor(block, path);
					tasks.push(code);
				}
			});
			return tasks;
		}
	}
})();