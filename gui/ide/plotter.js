let PLOTTER_LIMIT = 200;

let Plotter = (function () {

  let html = document.getElementById("plotter-canvas");
  let ctx = html.getContext("2d");
  let palette = ['#e41a1c','#377eb8','#4daf4a','#984ea3','#ff7f00','#ffff33','#a65628','#f781bf'];
  let series = [];
  let bounds = { x: 0, y: 0, w: 0, h: 0 };
  let observed = new Set();

  function init() {
    startStepping();
  }

  function resize() {
    let parent = html.parentNode;
    html.width = parent.offsetWidth - parent.offsetLeft;
    html.height = parent.offsetHeight - parent.offsetTop;
  }

  function startStepping() {
    function step() {
      updateBounds();
      draw();
      window.requestAnimationFrame(step);
    }
    window.requestAnimationFrame(step);
  }

  function updateBounds() {
    let x_min = Infinity;
    let y_min = Infinity;
    let x_max = -Infinity;
    let y_max = -Infinity;
    for (let i = 0; i < series.length; i++) {
      let s = series[i];
      for (let j = 0; j < s.data.length; j++) {
        let d = s.data[j];
        if (d.x < x_min) { x_min = d.x; }
        if (d.x > x_max) { x_max = d.x; }
        if (d.y < y_min) { y_min = d.y; }
        if (d.y > y_max) { y_max = d.y; }
      }
    }
    bounds.x = x_min;
    bounds.y = y_min;
    bounds.w = x_max - x_min;
    bounds.h = y_max - y_min;
  }

  function updateLabels() {
    let $labels = $("#plotter-labels");
    $labels.html("");
    for (let i = 0; i < series.length; i++) {
      let s = series[i];
      $labels.append($("<span>")
        .addClass("px-3 py-2 text-nowrap")
        .append($("<i>")
          .addClass("fas fa-square pr-1")
          .css("color", s.color))
        .append($("<span>").text(s.label)));
    }
  }

  function dataToPx(point) {
    let x = ((point.x - bounds.x) / bounds.w) * html.width;
    let y = html.height - ((point.y - bounds.y) / bounds.h) * html.height;
    return { x: x, y: y };
  }

  function drawSeries() {
      ctx.lineWidth = 2;
      ctx.setLineDash([]);
      for (let i = 0; i < series.length; i++) {
        let s = series[i];
        ctx.strokeStyle = s.color;
        ctx.beginPath();

        for (let j = 0; j < s.data.length; j++) {
          let p = dataToPx(s.data[j]);
          ctx.lineTo(p.x, p.y);
        }
        ctx.stroke();
      }
  }

  function drawAxisLabels() {
    ctx.fillStyle="white";
    ctx.textBaseline = "top";
    ctx.fillText((bounds.y + bounds.h).toFixed(2), 5, 0);
    ctx.textBaseline = "bottom";
    ctx.fillText(bounds.y.toFixed(2), 5, html.height);
  }

  function drawOrigin() {
    if (0 > bounds.y && 0 < (bounds.y + bounds.h)) {
      ctx.strokeStyle = "white";
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.setLineDash([2, 4]);
      let p = dataToPx({x: 0, y: 0});
      ctx.moveTo(0, p.y);
      ctx.lineTo(html.width, p.y);
      ctx.stroke();

      ctx.fillStyle="white";
      ctx.textBaseline = "bottom";
      ctx.fillText("0", 5, p.y);
    }
  }

  function draw() {
    ctx.clearRect(0, 0, html.width, html.height);
    drawSeries();
    drawAxisLabels();
    drawOrigin();
  }

  function update(state, old) {
    if (!old.isConnected && state.isConnected) {
      series.forEach(s => { s.data = []; });
    }
    updateValues(state.pins.elements);
    updateValues(state.globals.elements);
  }

  function updateValues(values) {
    let timestamp = +new Date(); // TODO(Richo): Use timestamp from VM
    for (let i = 0; i < values.length; i++) {
      let val = values[i];
      if (observed.has(val.name)) {
        let s = series.find(each => each.label == val.name);
        if (s != undefined) {
          s.data.push({
            x: timestamp,
            y: val.value
          });

          if (s.data.length > PLOTTER_LIMIT) {
            s.data.splice(0, s.data.length - PLOTTER_LIMIT);
          }
        }
      }
    }
  }

  function toggle(observable) {
    if (observed.has(observable)) {
      series = series.filter(each => each.label != observable);
      observed.delete(observable);
    } else {
      let colors = palette.filter(c => !series.some(s => s.color == c));
      if (colors.length == 0) { colors = palette; }
      series.push({
        label: observable,
        color: colors[Math.floor(Math.random() * colors.length)],
        data: []
      });
      observed.add(observable);
    }
    updateLabels();
    resize();
  }

  function colorFor(observable) {
    if (!observed.has(observable)) return null;
    let s = series.find(each => each.label == observable);
    return s.color;
  }

  return {
    init: init,
    resize: resize,
    update: update,
    toggle: toggle,
    colorFor: colorFor
  }

})();
