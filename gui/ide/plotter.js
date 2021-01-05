let Plotter = (function () {

  let html = document.getElementById("plotter-canvas");
  let ctx = html.getContext("2d");
  let palette = ['#e41a1c','#377eb8','#4daf4a','#984ea3','#ff7f00','#ffff33','#a65628','#f781bf','#999999'];
  let series = [];
  let bounds = { x: 0, y: 0, w: 0, h: 0 };

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
      updateLabels();
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

  // TODO(Richo): This function is just for testing purposes, delete it!
  (function testData() {
    series = [];
    let labels = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
    for (let i = 0; i < 9; i++) {
      let s = {
        label: labels[i],
        color: palette[i],
        data: []
      }
      for (let x = 0; x < 1000; x++) {
        s.data.push({ x: x, y: 0 });
      }
      series.push(s);
    }

    setInterval(function() {
      for (let i = 0; i < series.length; i++) {
        let s = series[i];
        let d = {
          x: s.data[s.data.length - 1].x + 1,
          y: s.data[s.data.length - 1].y + Math.random() * 10 - 5,
        }
        s.data.push(d);
        if (s.data.length > 1000) {
          s.data.shift(1);
        }
      }
    }, 16);
  })()

  return {
    init: init,
    resize: resize,
  }

})();
