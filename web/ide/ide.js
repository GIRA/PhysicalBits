let IDE = (function () {

  let selectedPort = "automatic";
  let blocklyArea, blocklyDiv, workspace;
  let IDE = {
    init: function () {
      initializeLayout();
      initializeConnectionPanel();
      initializeVisualPanel();
    }
  };

  function initializeLayout() {
    let config = {
      settings: {
        showPopoutIcon: false,
        showMaximiseIcon: false,
        showCloseIcon: false,
      },
      content: [{
        type: 'row',
        content:[{
          type: 'component',
          componentName: 'ide',
          componentState: { id: '#connection-panel' },
          title: 'Connection',
          width: 20,
        },{
          type: 'column',
          content:[{
            type: 'row',
            content: [{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#blocks-panel' },
              title: 'Blocks'
            },{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#code-panel' },
              title: 'Code'
            }]
          },{
            type: 'stack',
            height: 30,
            content: [{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#test3' },
              title: 'Output',
            },{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#test3' },
              title: 'Serial Monitor',
            },{
              type: 'component',
              componentName: 'ide',
              componentState: { id: '#test3' },
              title: 'Debugger',
            }]
          }]
        }]
      }]
    };

    let layout = new GoldenLayout(config, "#container");
    layout.registerComponent('ide', function(container, state) {
        let $el = $(state.id);
        container.getElement().append($el);
    });

    function updateSize() {
      let w = window.innerWidth;
      let h = window.innerHeight - $("#top-bar").outerHeight();
      if (layout.width != w || layout.height != h) {
        layout.updateSize(w, h);
      }
    };

    window.onresize = updateSize;
    layout.on('stateChanged', updateSize);
    layout.on('stateChanged', resize);
    layout.init();
    updateSize();
  }

  function initializeConnectionPanel() {
    $("#port-dropdown").change(choosePort);
    $("#connect-button").on("click", connect);
    $("#disconnect-button").on("click", disconnect);
    Uzi.addObserver(updateConnectionPanel);
  }

  function initializeVisualPanel() {
    blocklyArea = $("#visual-editor").get(0);
    blocklyDiv = $("#blockly").get(0);
    initBlockly();
  }

  function initBlockly() {
    var counter = 0;
    ajax.request({
      type: 'GET',
      url: 'toolbox.xml',
      success: function (toolbox) {
        workspace = Blockly.inject(blocklyDiv, { toolbox: toolbox });
        makeResizable();
        if (++counter == 2) { restore(); }
      }
    });

    ajax.request({
      type: 'GET',
      url: 'blocks.json',
      success: function (json) {
        let blocks = JSON.parse(json);
        Blockly.defineBlocksWithJsonArray(blocks);
        if (++counter == 2) {	restore(); }
      }
    });
  }

  function resize() {
    // Only if Blockly was initialized
    if (blocklyDiv == undefined || blocklyArea == undefined) return;
    let x, y;
    x = y = 0;
    blocklyDiv.style.left = x + 'px';
    blocklyDiv.style.top = y + 'px';
    blocklyDiv.style.width = blocklyArea.offsetWidth + 'px';
    blocklyDiv.style.height = blocklyArea.offsetHeight + 'px';
    Blockly.svgResize(workspace);
  }

  function makeResizable() {
    var onresize = function (e) { resize(); }
    window.addEventListener('resize', onresize, false);
    onresize();
  }

	function restore() {
		try {
			Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(localStorage["uzi"]), workspace);
		} catch (err) {
			console.error(err);
		}
		workspace.addChangeListener(function workspaceChanged() {
  		// save();
  		// scheduleAutorun();
  	});
	}

  function choosePort() {
    let value = $("#port-dropdown").val();
    if (value == "other") {
      let defaultOption = selectedPort == "automatic" ? "" : selectedPort;
      value = prompt("Port name:", defaultOption);
      if (!value) { value = selectedPort; }
    }
    setSelectedPort(value);
  }

  function setSelectedPort(val) {
    selectedPort = val;
    if ($("#port-dropdown option[value='" + selectedPort + "']").length <= 0) {
      $("<option>")
        .text(selectedPort)
        .attr("value", selectedPort)
        .insertBefore("#port-dropdown-divider");
    }
    $("#port-dropdown").val(selectedPort);
  }

  function connect() {
    $("#connect-button").attr("disabled", "disabled");
    Uzi.connect(selectedPort);
  }

  function disconnect() {
    $("#disconnect-button").attr("disabled", "disabled");
    Uzi.disconnect();
  }

  function updateConnectionPanel() {
    if (Uzi.state.isConnected) {
      $("#connect-button").hide();
      $("#disconnect-button").show();
      $("#disconnect-button").attr("disabled", null);
      $("#port-dropdown").attr("disabled", "disabled");
      setSelectedPort(Uzi.state.portName);
    } else {
      $("#disconnect-button").hide();
      $("#connect-button").show();
      $("#connect-button").attr("disabled", null);
      $("#port-dropdown").attr("disabled", null);
    }
  }

  return IDE;
})();
