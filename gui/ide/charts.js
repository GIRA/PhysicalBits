
let html = document.getElementById("plotter-canvas");
let ctx = html.getContext("2d");
//let palette = ["#4477AA", "#66CCEE", "#228833", "#CCBB44", "#EE6677", "#AA3377", "white"];
//let palette = ["#0077BB", "#33BBEE", "#009988", "#EE7733", "#CC3311", "#EE3377", "#BBBBBB"];
let palette = ['#e41a1c','#377eb8','#4daf4a','#984ea3','#ff7f00','#ffff33','#a65628','#f781bf','#999999'];

function resize() {
  let parent = html.parentNode;
  html.width = parent.offsetWidth - parent.offsetLeft;
  html.height = parent.offsetHeight - parent.offsetTop;
}

window.addEventListener("resize", resize);
resize();


function initializeStepping() {
  function step() {
    // TODO(Richo): This shouldn't be necessary if we tie the resize to the layout state change
    resize();
    updateLabels();
    draw();
    window.requestAnimationFrame(step);
  }
  window.requestAnimationFrame(step);
}

initializeStepping();

let series = [];
let labels = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
for (let i = 0; i < 9; i++) {
  let s = {
    label: labels[i],
    color: palette[i],
    data: []
  }
  let y = Math.random() * 100;
  for (let j = 0; j < 10; j++) {
    s.data.push(y);
    y += Math.random() * 10 - 5;
  }
  series.push(s);
}

setInterval(function() {
  for (let i = 0; i < series.length; i++) {
    let s = series[i];
    s.data.push(s.data[s.data.length - 1] + Math.random() * 10 - 5);
    if (s.data.length > 1000) {
      s.data.shift(1);
    }
  }
}, 16);

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

function draw() {
  /*
  ctx.fillStyle = "blue";
  ctx.fillRect(0, 0, html.width, html.height);
  */


  let y_min = Math.min.apply(null, series.map(s => Math.min.apply(null, s.data)))
  let y_max = Math.max.apply(null, series.map(s => Math.max.apply(null, s.data)))

  ctx.lineWidth = 2;
  ctx.setLineDash([]);
  for (let i = 0; i < series.length; i++) {
    let s = series[i];
    ctx.strokeStyle = s.color;
    ctx.beginPath();

    //ctx.moveTo(0, 0);
    for (let j = 0; j < s.data.length; j++) {
      let x = j * (html.width/s.data.length);
      let y = html.height - ((s.data[j] - y_min) / (y_max - y_min)) * html.height;
      ctx.lineTo(x, y);
    }
    ctx.stroke();
  }

  ctx.fillStyle="white";
  //ctx.font = '48px serif';
  ctx.textBaseline = "top";
  ctx.fillText(y_max.toFixed(2), 5, 0);
  ctx.textBaseline = "bottom";
  ctx.fillText(y_min.toFixed(2), 5, html.height);

  if (0 > y_min && 0 < y_max) {
    ctx.strokeStyle = "white";
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.setLineDash([2, 4]);
    let y = html.height - ((0 - y_min) / (y_max - y_min)) * html.height;
    ctx.moveTo(0, y);
    ctx.lineTo(html.width, y);
    ctx.stroke();

    ctx.fillStyle="white";
    ctx.textBaseline = "bottom";
    ctx.fillText("0", 5, y);
  }
}
