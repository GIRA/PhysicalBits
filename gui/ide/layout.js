
let LayoutManager = (function () {

  let layout, defaultLayoutConfig;
  let onStateChanged = function () { /* DO NOTHING */ }

  function init(callback) {
    if (callback) { onStateChanged = callback; }
    return loadDefaultLayoutConfig().then(initializeDefaultLayout);
  }

  function reset() {
    return initializeDefaultLayout();
  }

  function loadDefaultLayoutConfig() {
    return ajax.GET("default-layout.json")
      .then(function (data) { defaultLayoutConfig = data; });
  }

  function initializeDefaultLayout() {
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

  return {
    init: init,
    reset: reset,

    isBroken: isBroken,
    getLayoutConfig: getLayoutConfig,
    setLayoutConfig: setLayoutConfig,
  };

})();
