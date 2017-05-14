var UziBlock = (function () {

	function initToolbox(toolbox) {		
		var blocklyArea = document.getElementById('editor');
		var blocklyDiv = document.getElementById('blockly');
		var workspace = Blockly.inject(blocklyDiv, { toolbox: toolbox });
		var onresize = function(e) {
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
		};
		window.addEventListener('resize', onresize, false);
		onresize();
		Blockly.svgResize(workspace);		
	}
	
	function initBlocks(blocks) {		
		Blockly.defineBlocksWithJsonArray(blocks);
	}
	
	ajax.request({
		type: 'GET',
		url: 'toolbox.xml',
		success: function (xml) {
			initToolbox(xml);
		}
	});
	
	ajax.request({
		type: 'GET',
		url: 'blocks.json',
		success: function (json) {
			initBlocks(JSON.parse(json));
		}
	});
	
})();