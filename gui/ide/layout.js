
let LayoutManager = (function () {

  let defaultLayoutConfig = {
    "settings": {
      "showPopoutIcon": false,
      "showMaximiseIcon": false,
      "showCloseIcon": false
    },
    "content": [{
      "type": "row",
      "content":[/*{
        "type": "column",
        "width": 17,
        "content": [{
          "id": "controls",
          "type": "component",
          "height": 30,
          "componentName": "DOM",
          "componentState": { "id": "#controls-panel" },
          "title": "Controls"
        },{
          "id": "inspector",
          "type": "component",
          "componentName": "DOM",
          "componentState": { "id": "#inspector-panel" },
          "title": "Inspector"
        }]
      },*/{
        "id": "blocks",
        "type": "component",
        "componentName": "DOM",
        "componentState": { "id": "#blocks-panel" },
        "title": "Blocks"
      },{
        "type": "column",
        "width": 25,
        "content":[{
          "id": "code",
          "type": "component",
          "componentName": "DOM",
          "componentState": { "id": "#code-panel" },
          "title": "Code"
        },{
          "id": "output",
          "type": "component",
          "height": 30,
          "componentName": "DOM",
          "componentState": { "id": "#output-panel" },
          "title": "Output"
        }]
      }]
    }]
  };
  let plotterConfig = {
    "id": "plotter",
    "type": "component",
    "height": 30,
    "componentName": "DOM",
    "componentState": { "id": "#plotter-panel" },
    "title": "Plotter"
  };

  let layout;
  let onStateChanged = function () { /* DO NOTHING */ }

  function init(callback) {
    if (callback) { onStateChanged = callback; }
    return new Promise(resolve => {
      reset();
      resolve();
    });
  }

  function reset() {
    setLayoutConfig(defaultLayoutConfig);
  }

  function getLayoutConfig() { return layout.toConfig(); }

  function setLayoutConfig(config) {
    if (layout) { layout.destroy(); }

    layout = new GoldenLayout(config, "#layout-container");
    layout.registerComponent('DOM', function(container, state) {
      let $el = $(state.id);
      container.getElement().append($el);
      container.on('destroy', function () {
        $("#hidden-panels").append($el);
      });
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
    layout.on('stateChanged', onStateChanged);
    layout.init();
    updateSize();
  }

  function isBroken() {
    return layout.config.content.length == 0;
  }

  function findBiggestComponent() {
    let items = layout.root.getItemsByType("component")
      .map(item => ({ id: item.config.id, size: item.container.width*item.container.height }));
    items.sort((a, b) => b.size - a.size);
    return items[0].id;
  }

  function showPlotter() {
    if (layout.root.getItemsById("plotter").length > 0) return;

    let siblingPanel = layout.root.getItemsById(findBiggestComponent())[0];
    let path = [siblingPanel];
    do {
      path.unshift(path[0].parent);
    } while (path[0].type == "stack");
    let parent = path[0];
    if (parent.type == "column") {
      parent.addChild(plotterConfig);
    } else {
      let siblingConfig = path[1].config;
      siblingConfig.height = 100 - plotterConfig.height;
      parent.replaceChild(path[1], {
        type: "column",
        width: siblingConfig.width,
        content: [siblingConfig, plotterConfig]
      });
    }
  }

  return {
    init: init,
    reset: reset,
    showPlotter: showPlotter,
    isBroken: isBroken,
    getLayoutConfig: getLayoutConfig,
    setLayoutConfig: setLayoutConfig,
  };

})();
