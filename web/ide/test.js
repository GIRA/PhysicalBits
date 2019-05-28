
var config = {
    settings: {
      showPopoutIcon: false,
    },
    content: [{
        type: 'row',
        content:[{
            type: 'component',
            componentName: 'testComponent',
            componentState: { label: 'A', id: '#connectionPanel' },
            title: 'Connection',
            width: 20,
        },{
            type: 'column',
            content:[{
                type: 'component',
                componentName: 'testComponent',
                componentState: { label: 'B', id: '#test2' }
            },{
                type: 'component',
                componentName: 'testComponent',
                componentState: { label: 'C', id: '#test3' },
                title: 'prueba'
            }]
        }]
    }]
};

var myLayout = new GoldenLayout(config, "#container");

myLayout.registerComponent('testComponent', function( container, state ){
    /*
    let html = '<h1>' + state.id + '</h1>' +
              '<h2>' + state.label + '</h2>';
    container.getElement().html(html);
    */
    let $el = $(state.id);
    container.getElement().append($el);
});

myLayout.init();
window.onresize = function () { myLayout.updateSize(); };
