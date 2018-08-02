var UziBlock = (function () {
	
	var blocklyArea, blocklyDiv, workspace;
	var autorunEnabled = false;
	var lastFileName;
	
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
		
		$("#autorunCheckbox").on("change", function () {
			autorunEnabled = this.checked;
			autorun();
		});
		
		$("#openButton").on("change", function () {
			var file = this.files[0];
			this.value = null;
			if (file === undefined) return;
			
			var reader = new FileReader();			
			reader.onload = function(e) {
				var xml = e.target.result;
				var dom = Blockly.Xml.textToDom(xml);
				workspace.clear();
				Blockly.Xml.domToWorkspace(dom, workspace);
			};
			reader.readAsText(file);
		});
		
		$("#saveButton").on("click", function () {
			lastFileName = prompt("File name:", lastFileName || "program.xml");
			var xml = Blockly.Xml.domToPrettyText(Blockly.Xml.workspaceToDom(workspace));
			var blob = new Blob([xml], {type: "text/plain;charset=utf-8"});
			saveAs(blob, lastFileName);
		});

		Uzi.onUpdate(function () {
			if (Uzi.isConnected) {				
				$("#install").removeAttr("disabled");
				$("#run").removeAttr("disabled");
				$("#more").removeAttr("disabled");
				autorun();
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
		var xml = Blockly.Xml.workspaceToDom(workspace);
		return CodeGenerator.generate(xml);
	}
	
	function getGeneratedCodeAsJSON() {
		var code = getGeneratedCode();
		return JSON.stringify(code);
	}
	
	function workspaceChanged() {
		save();
		autorun();
	}
	
	function save() {
		localStorage["uzi"] = Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(workspace));
	}
	
	function autorun() {
		if (autorunEnabled && Uzi.isConnected) {
			var old = Uzi.currentProgram;
			var cur = getGeneratedCodeAsJSON();
			if (old === undefined || old.src !== cur) {
				Uzi.run(cur, "json", function (bytecodes) {			
					console.log(bytecodes);
				});
			}
		}
	}
	
	function restore(){
		Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(localStorage["uzi"]), workspace);
		workspace.addChangeListener(workspaceChanged);
	}
	
	return {
		init: init,
		getGeneratedCode: getGeneratedCode,
		getWorkspace: function () { return workspace; }
	};
	
})();