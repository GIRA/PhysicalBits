$(document).ready(function () {
  var config = {
      settings: {
        showPopoutIcon: false,
        showMaximiseIcon: false,
        showCloseIcon: false,
      },
      content: [{
          type: 'row',
          content:[{
              type: 'component',
              componentName: 'testComponent',
              componentState: { label: 'A', id: '#connectionPanel' },
              title: 'Connection',
              width: 15,
          },{
              type: 'column',
              content:[{
                  type: 'component',
                  componentName: 'testComponent',
                  componentState: { label: 'B', id: '#test2' },
              },{
                  type: 'component',
                  componentName: 'testComponent',
                  componentState: { label: 'C', id: '#test3' },
                  title: 'prueba',
              }]
          }]
      }]
  };

  var layout = new GoldenLayout(config, "#container");

  layout.registerComponent('testComponent', function(container, state) {
      let $el = $(state.id);
      container.getElement().append($el);
  });

  function updateSize() {
    let w = window.innerWidth;
    let h = window.innerHeight - $("#top-bar").height();
    if (layout.width != w || layout.height != h) {
      layout.updateSize(w, h);
    }
  };

  window.onresize = updateSize;
  layout.on('stateChanged', updateSize);
  layout.init();
  updateSize();
});
