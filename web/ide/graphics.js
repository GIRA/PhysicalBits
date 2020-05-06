function ctorGraphics()
{
  let graphics = {
    drawCircles: drawCircles,
    showGlobals: showGlobals
  };
  return graphics;
}

function drawCircles(target,radius, simulator) {
  target.innerHTML="";
  let x= 0;
  let index = 0;
  for( let i=0;i<simulator.pins.length;i++) {
    x+=radius*2 + 10;
    let c = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    let t = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    c.setAttribute("fill", "blue");
    c.setAttribute("cx", x );
    c.setAttribute("cy", radius);
    c.setAttribute("r", radius);
    t.setAttribute("x", x - radius);
    t.setAttribute("y",(radius + 40));
    t.setAttribute( "font-size", "25");
    t.setAttribute("font-family", "sans-serif");
    t.setAttribute( "width", "50");
    t.setAttribute("height", "50");
    t.textContent = "D" + index++;
    t.setAttribute('fill', '#000');
    t.setAttribute("viewBox", "0 0 1000 300");
    target.appendChild(c);
    target.appendChild(t);
    setInterval(() => {
      if(simulator.pins[i]>=0.5) {
          c.setAttribute("fill", "chartreuse");
      } else {
          c.setAttribute("fill", "black");
      }
    }, 1);
  }
}

function showGlobals(simulator)
{
  var table = document.getElementById("globalsTable");
  if(table.rows.length==0){

      for(let global in simulator.globals){
        var row = table.insertRow(0);
        var cell1= row.insertCell(0);
        var cell2= row.insertCell(1);
        cell1.textContent = global;
        cell2.textContent = simulator.globals[global];
    }
  } else{
    let index = 0;
    for(let global in simulator.globals){
        table.rows[index].cells[1].textContent = simulator.globals[global];
     }  
  }
}


