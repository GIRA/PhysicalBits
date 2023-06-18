
let LayoutManager = (function () {

  let components = {
    controls: {
      "id": "controls",
      "type": "component",
      "height": 30,
      "componentName": "DOM",
      "componentState": { "id": "#controls-panel" },
      "title": "Controls"
    },
    blocks: {
      "id": "blocks",
      "type": "component",
      "componentName": "DOM",
      "componentState": { "id": "#blocks-panel" },
      "title": "Blocks"
    },
    code: {
      "id": "code",
      "type": "component",
      "componentName": "DOM",
      "componentState": { "id": "#code-panel" },
      "title": "Code"
    },
    inspector: {
      "id": "inspector",
      "type": "component",
      "componentName": "DOM",
      "componentState": { "id": "#inspector-panel" },
      "title": "Inspector"
    },
    output: {
      "id": "output",
      "type": "component",
      "height": 30,
      "componentName": "DOM",
      "componentState": { "id": "#output-panel" },
      "title": "Output"
    },
    plotter: {
      "id": "plotter",
      "type": "component",
      "height": 30,
      "componentName": "DOM",
      "componentState": { "id": "#plotter-panel" },
      "title": "Plotter"
    },
    debugger: {
      "id": "debugger",
      "type": "component",
      "height": 30,
      "componentName": "DOM",
      "componentState": { "id": "#debugger-panel" },
      "title": "Debugger"
    }, 
  };

  let defaultContent = {
    "type": "row",
    "content":[{
      "type": "column",
      "width": 17,
      "content": [
        components.controls,
        components.inspector
      ]
    }, {
      "type": "column",
      "width": 58,
      "content": [
        components.blocks
      ]
    },
    {
      "type": "column",
      "width": 25,
      "content":[
        components.code,
        components.output
      ]
    }]
  };

  let contentWithoutBlocks = {
    "type": "row",
    "content":[{
      "type": "column",
      "width": 17,
      "content": [
        components.controls,
        components.inspector
      ]
    },    
    components.code,
    {
      "type": "column",
      "width": 25,
      "content":[
        components.output
      ]
    }]
  };
  
  let contentWithoutCode = {
    "type": "row",
    "content":[{
      "type": "column",
      "width": 17,
      "content": [
        components.controls,
        components.inspector
      ]
    },
    components.blocks,
    {
      "type": "column",
      "width": 25,
      "content":[
        components.output
      ]
    }]
  };

  let settings = {
    "showPopoutIcon": false,
    "showMaximiseIcon": false,
    "showCloseIcon": false
  };

  let layout;
  let panels = new Map();
  let onStateChanged = function () { /* DO NOTHING */ }
  let resetting = false;
  
  let observers = {
    "close" : [],
  };

  function init(callback) {
    if (callback) { onStateChanged = callback; }
    return new Promise(resolve => {
      reset();
      resolve();
    });
  }

  function getDefaultContent() {
    let removeFrom = (component, col) => {
      let idx = col.indexOf(component);
      if (idx !== -1) { col.splice(idx, 1); }
    };
    let moveFromTo = (component, col_from, col_to) => {
      removeFrom(component, col_from);
      col_to.push(component);
    };

    let includeBlocks = Uzi.state.features["blocks?"];
    let includeCode = Uzi.state.features["code?"];
    let includeInspector = Uzi.state.features["monitoring?"];
    includeInspector &= Uzi.state.features["interactivity?"];
    
    let left = [components.controls, components.inspector];
    let main = [components.blocks];
    let right = [components.code, components.output];
    
    if (!includeCode) {
      removeFrom(components.code, right);
    } else if (!includeBlocks) {
      removeFrom(components.blocks, main);
      moveFromTo(components.code, right, main);
    }

    if (!includeInspector) {
      removeFrom(components.inspector, left);
      moveFromTo(components.output, right, left);
      components.output.height = 70;
    }

    let content = [{
      "type": "column",
      "width": 17,
      "content": left
    }, {
      "type": "column",
      "width": 58,
      "content": main
    }, {
      "type": "column",
      "width": 25,
      "content": right
    }];

    if (right.length == 0) {
      content.pop();
      content[1]["width"] = 83;
    }

    return content;
  }

  function getBasicContent() {
    let left = [components.controls, components.inspector];
    let main = [components.blocks];

    let content = [{
      "type": "column",
      "width": 17,
      "content": left
    }, {
      "type": "column",
      "width": 83,
      "content": main
    }];

    return content;
  }

  function reset() {    
    Uzi.elog("LAYOUT/RESET");
    setLayoutConfig({
      settings: settings,
      content: [{
        type: "row",
        content: getDefaultContent()
      }]
    });
  }
  
  function setBasicContent() {    
    Uzi.elog("LAYOUT/RESET BASIC CONTENT");
    setLayoutConfig({
      settings: settings,
      content: [{
        type: "row",
        content: getBasicContent()
      }]
    });
  }

  function setAdvancedContent() {    
    Uzi.elog("LAYOUT/RESET ADVANCED CONTENT");
    setLayoutConfig({
      settings: settings,
      content: [{
        type: "row",
        content: getDefaultContent()
      }]
    });
  }

  function on (evt, callback) {
    observers[evt].push(callback);
  }

  function trigger(evt, args) {
    observers[evt].forEach(function (fn) {
      try {
        fn(args);
      } catch (err) {
        console.log(err);
      }
    });
  }

  function getLayoutConfig() { return layout.toConfig(); }

  function update(config, fn) {
    if (config instanceof Array) return config.map(e => update(e, fn));
    if (typeof config != "object") return config;
    if (config === null) return null;
    if (config === undefined) return undefined;
  
    let value = {};
    for (let m in config) {
      value[m] = update(config[m], fn);
    }
    fn(value);
    return value;
  }

  function updateClosable(config) {
    let isClosable = Uzi.state.features["closable-panels?"];
    return update(config, value => {
      if (value["type"] == "component") {
        value["isClosable"] = isClosable;
      }
    });
  }

  function setLayoutConfig(config) {
    resetting = true;
    setTimeout(() => resetting = false, 0);

    if (layout) { layout.destroy(); }
    panels.clear();

    layout = new GoldenLayout(updateClosable(config), "#layout-container");
    layout.registerComponent('DOM', function(container, state) {
      let $el = $(state.id);
      container.getElement().append($el);
      container.on('destroy', function () {
        $("#hidden-panels").append($el);
        trigger("close", state.id);
        if (!resetting) {
          Uzi.elog("LAYOUT/PANEL_CLOSE", state.id);
        }
      });
      panels[state.id] = container;
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

  function getPanel(id) {
    return panels[id];
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

  function showPanel(name) {
    if (layout.root.getItemsById(name).length > 0) return;
    // Uzi.elog("LAYOUT/PANEL_OPEN #", id, "-panel");

    let siblingPanel = layout.root.getItemsById(findBiggestComponent())[0];
    let path = [siblingPanel];
    do {
      path.unshift(path[0].parent);
    } while (path[0].type == "stack");
    let parent = path[0];
    console.log(parent);

    let config = components[name];
    console.log(config);
    if (parent.type == "column") {
      parent.addChild(config);
    } else {
      let siblingConfig = path[1].config;
      siblingConfig.height = 100 - config.height;
      console.log(siblingConfig);
      parent.replaceChild(path[1], {
        type: "column",
        width: siblingConfig.width,
        content: [siblingConfig, config],
        config: config
      });
    }
  }

  function closePanel(id) {
    let panel = getPanel(id);
    if (panel) { panel.close(); }
  }

  return {
    init: init,
    reset: reset,
    setBasicContent: setBasicContent,
    setAdvancedContent: setAdvancedContent,
    on: on,
    showPanel: showPanel,
    isBroken: isBroken,
    getLayoutConfig: getLayoutConfig,
    setLayoutConfig: setLayoutConfig,
    getPanel: getPanel,
    closePanel: closePanel,
  };

})();
