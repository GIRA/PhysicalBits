
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
  
  let preferredPaths = {};
  
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

  function reset(advanced) {    
    if (advanced){
      LayoutManager.setAdvancedContent();
    } else {
      LayoutManager.setBasicContent();
    }
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
  
  function showPanel(name) {
    if (layout.root.getItemsById(name).length > 0) return;
    
    let preferredPath = preferredPaths[name];
    if (!preferredPath) {
      console.log("NO PREFERRED PATH!");
      preferredPath = [0, 1, 10];
    } else {
      console.log(preferredPath);
    }
    let simpleLayout = simplifyLayout(getLayoutConfig());
    let newLayout = insertIn(simpleLayout, preferredPath, name);
    setLayoutConfig(complicateLayout(newLayout));
  }


  function hidePanel(name) {
    let simpleLayout = simplifyLayout(getLayoutConfig());
    let path = getPath(name, simpleLayout);
    preferredPaths[name] = path;
    let id = components[name].componentState.id;
    closePanel(id);
  }

  function closePanel(id) {
    let panel = getPanel(id);
    if (panel) { panel.close(); }
  }

  function simplifyLayout(layout) {
    if (layout.type == "stack" && layout.content.length == 1) {
      let content = layout.content[0];
      if (layout.width && !content.width) {
        content.width = layout.width;
      }
      if (layout.height && !content.height) {
        content.height = layout.height;
      }
      return simplifyLayout(content);
    } else {
      let content = [];
      if (layout.content) {
        content = layout.content.map(e => simplifyLayout(e));
      }
      if (layout.id) {
        return layout.id;
      } else {
        let result = {};
        result.content = content;
        if (layout.type) { result.type = layout.type; }
        if (layout.width) { result.width = layout.width; }
        if (layout.height) { result.height = layout.height; }
        return result;
      }
    }
  }

  function complicateLayout(layout) {
    if (typeof layout === 'string' || layout instanceof String) {
      var component = components[layout];
      return {
        type: "stack",
        width: component.width || 50,
        height: component.height || 50,
        content: [component]
      };
    } else {
      let result = {};
      if (layout.content) {
        result.content = layout.content.map(complicateLayout);
      }
      if (layout.type) {
        result.type = layout.type;
      }
      if (layout.width) {
        result.width = layout.width;
      }
      if (layout.heigth) {
        result.height = layout.height;
      }
      return result;
    }
  }

  function getPath(element, layout) {
    if (element == layout) return [];
    if (typeof layout === 'string' || layout instanceof String) {
      return null;
    }
    for (let i = 0; i < layout.content.length; i++) {
      var v = layout.content[i];
      if (v.content && v.content.length == 1 && v.content[0] == element) {
        return [i];
      }      
      let path = getPath(element, v);
      if (path) return [i].concat(path);
    }
  }

  function insertIn(layout, path, element, parent) {
    if (typeof layout === 'string' || layout instanceof String) {
      let idx = path[0];
      return {
        type: parent == "row" ? "column" : "row",
        content: idx == 0 ? [element, layout] : [layout, element]
      };
    }
    let result = {};
    if (layout.type) result.type = layout.type;
    if (layout.width) result.width = layout.width;
    if (layout.heigth) result.height = layout.height;
    let idx = path[0];
    path = path.slice(1);
    let content = layout.content;
    if (!content || content.length == 0) {
      result.content = [element];
    } else if (idx >= content.length) {
      result.content = content.concat(element);
    } else if (path.length == 0) {
      let l = content.slice(0, idx);
      let r = content.slice(idx);
      result.content = l.concat(element).concat(r);
    } else {
      result.content = [];
      for (let i = 0; i < content.length; i++) {
        if (i == idx) {
          result.content.push(insertIn(content[i], path, element, layout.type));
        } else {
          result.content.push(content[i]);
        }
      }
    }
    return result;
  }

  return {
    init: init,
    reset: reset,
    setBasicContent: setBasicContent,
    setAdvancedContent: setAdvancedContent,
    on: on,
    showPanel: showPanel,
    hidePanel: hidePanel,
    isBroken: isBroken,
    getLayoutConfig: getLayoutConfig,
    setLayoutConfig: setLayoutConfig,
    getPanel: getPanel,
    closePanel: closePanel,
  };

})();
