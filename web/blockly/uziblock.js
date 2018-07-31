var UziBlock = (function () {
	
	var blocklyArea, blocklyDiv, workspace;	
	
	function init(area, div) {
		blocklyArea = area;
		blocklyDiv = div;
		
		initButtons();
		initBlockly();
	}
	
	function initButtons() {	
		$("#compile").on("click", function () {
			Uzi.compile(getGeneratedCodeAsJSON(), "json", function (bytecodes) {			
				console.log(bytecodes);
				Alert.success("Compilation successful");
			});
		});

		$("#install").on("click", function () {
			Uzi.install(getGeneratedCodeAsJSON(), "json", function (bytecodes) {			
				console.log(bytecodes);
				Alert.success("Installation successful");
			});
		});

		$("#run").on("click", function () {
			Uzi.run(getGeneratedCodeAsJSON(), "json", function (bytecodes) {			
				console.log(bytecodes);
			});
		});

		Uzi.onUpdate(function () {
			if (Uzi.isConnected) {				
				$("#install").removeAttr("disabled");
				$("#run").removeAttr("disabled");
				$("#more").removeAttr("disabled");
			} else {
				$("#install").attr("disabled", "disabled");
				$("#run").attr("disabled", "disabled");
				$("#more").attr("disabled", "disabled");
			}
		});
	}
	
	function initBlockly() {		
		var counter = 0;
		ajax.request({
			type: 'GET',
			url: 'toolbox.xml',
			success: function (xml) {
				initToolbox(xml);
				makeResizable();
				if (++counter == 2) {
					restore();
				}
			}
		});
		
		ajax.request({
			type: 'GET',
			url: 'blocks.json',
			success: function (json) {
				initBlocks(JSON.parse(json));
				if (++counter == 2) {
					restore();
				}
			}
		});
	}
	
	function initToolbox(toolbox) {
		workspace = Blockly.inject(blocklyDiv, { toolbox: toolbox });
	}
	
	function initBlocks(blocks) {		
		Blockly.defineBlocksWithJsonArray(blocks);
	}	
	
	function makeResizable() {
		var onresize = function (e) {
			// Compute the absolute coordinates and dimensions of blocklyArea.
			var element = blocklyArea;
			var x = 0;
			var y = 0;
			do {
			  x += element.offsetLeft;
			  y += element.offsetTop;
			  element = element.offsetParent;
			} while (element);
			// Position blocklyDiv over blocklyArea.
			blocklyDiv.style.left = x + 'px';
			blocklyDiv.style.top = y + 'px';
			blocklyDiv.style.width = blocklyArea.offsetWidth + 'px';
			blocklyDiv.style.height = blocklyArea.offsetHeight + 'px';
		}
		window.addEventListener('resize', onresize, false);
		onresize();
		Blockly.svgResize(workspace);
	}
	
	function getGeneratedCode(){
		var wks = Blockly.getMainWorkspace();
		var xml = Blockly.Xml.workspaceToDom(wks);
		return CodeGenerator.generate(xml);
	}
	
	function getGeneratedCodeAsJSON() {
		var code = getGeneratedCode();
		return JSON.stringify(code);
	}
	
	function save() {
		localStorage["uzi"] = Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(workspace));
	}
	
	function restore(){
		Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(localStorage["uzi"]), workspace);
		workspace.addChangeListener(save);
	}
	
	return {
		init: init,
		getGeneratedCode: getGeneratedCode,
		getWorkspace: function () { return workspace; },
		save: save,
		restore: restore
	};
	
})();