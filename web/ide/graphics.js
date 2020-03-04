function ctorGraphics()
{
  let graphics = {
    drawCircles: drawCircles
  };
  return graphics;
}

function drawCircles(target,radius, simulator) {
  target.innerHTML="";
  let x= 0;
  let index = 0;
  for( let i=0;i<simulator.pins.length;i++) {
    x+=radius*2 + 10;
    let c =  document.createElementNS('http://www.w3.org/2000/svg', 'circle');
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
    }, 50);
  }
}

function writeGlobals()
{
      let t = document.createElementNS('http://www.w3.org/2000/svg', 'text');


}
