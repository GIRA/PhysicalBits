function ctorGraphics()
{
  let graphics = {
    drawCircles: drawCircles,
    createPins: createPins,
    showStats: showStats,
    createStackTable: createStackTable,
    displaySpeed: displaySpeed
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
    c.addEventListener("mousedown", function(event){
      if(simulator.pins[i]==0){
        simulator.pins[i]=1;
      } else{
        simulator.pins[i]=0;
      }
    });
    setInterval(() => {
      if(simulator.pins[i]>=0.5) {
          c.setAttribute("fill", "#a6e369");
      } else{
          c.setAttribute("fill", "#0e141b");
      }
    }, 1);
  }
}

function createBezier(target){
  var path = document.createElementNS('http://www.w3.org/2000/svg','path');
  var circle = connector(55,300,25);
  var circle2 = connector(55,300,25);

  path.id="path";
  path.setAttribute("class", "path");

  handle=circle;
  handle2=circle2;
  path1 = path;

  updatePath();

  target.appendChild(circle);
  target.appendChild(circle2);
  target.appendChild(path);
}

function connector(x,y,r){
  var circle = document.createElementNS('http://www.w3.org/2000/svg','circle');
  circle.setAttribute("cx", x );
  circle.setAttribute("cy", y);
  circle.setAttribute("r", r);
  circle.id="handle";
  circle.setAttribute("class", "handle");
  drag(circle);
  return circle;
}

var handle;
var handle2;
var path1;

function updatePath() {
  
  var bezierWeight = 0.675;


  var x1 = handle2.getAttribute("cx");
  var y1 = handle2.getAttribute("cy");
  
  var x4 = handle.getAttribute("cx");
  var y4 = handle.getAttribute("cy");
  
  var dx = Math.abs(x4 - x1) * bezierWeight;
  
  var x2 = x1 - dx;
  var x3 = x4 + dx;
  
  var data = `M${x1} ${y1} C ${x2} ${y1} ${x3} ${y4} ${x4} ${y4}`;

  path1.setAttribute("d", data);
}

function getMousePosition(evt) {
  var CTM = svg.getScreenCTM();
  return {
    x: (evt.clientX - CTM.e) / CTM.a,
    y: (evt.clientY - CTM.f) / CTM.d
  };
}

function createBoard(target){
  var arduino_board = document.createElementNS('http://www.w3.org/2000/svg','image');
  arduino_board.setAttribute('height','300');
  arduino_board.setAttribute('width','300');
  arduino_board.setAttributeNS('http://www.w3.org/1999/xlink','href', 'Arduino_uno_board.png');
  arduino_board.setAttribute('x','500');
  arduino_board.setAttribute('y','0');
  arduino_board.setAttribute('class','handle');
  drag(arduino_board);
  target.appendChild(arduino_board);
}

function createLed(target){
  var led = document.createElementNS('http://www.w3.org/2000/svg','image');
  led.setAttributeNS(null,'height','150');
  led.setAttributeNS(null,'width','150');
  led.setAttributeNS('http://www.w3.org/1999/xlink','href', 'Sintitulo-2.png');
  led.setAttributeNS(null,'x','350');
  led.setAttributeNS(null,'y','70');
  led.setAttributeNS(null, 'visibility', 'visible');
  led.setAttribute('class','handle');
  drag(led);
  target.appendChild(led);
}

function showStats(sim,target){
  createBezier(target);
  createBoard(target);
  createLed(target);

  setInterval(() => {
    showGlobals(sim);
    updateStack(sim);
    updatePins(sim);
    showPc(sim);
    updatePath();
    }, 1);
}

function showGlobals(simulator){
  removeTable("globalsTable");
  createGlobalsTable(simulator);
}

function createGlobalsTable(simulator){
  var table = document.getElementById("globalsTable");
  for(let global in simulator.globals){
    var row = table.insertRow(0);
    var cell1= row.insertCell(0);
    var cell2= row.insertCell(1);
    cell1.textContent = global;
    cell2.textContent = simulator.globals[global];
  }
}

function removeTable(idTable){
  var table = document.getElementById(idTable);
  for(let i=table.rows.length;i>0;i--){
    table.deleteRow(i -1);
  } 
}

function updateStack(simulator){
  var table = document.getElementById("stackTable");
  var stack = simulator.stack;
  showStack(stack.slice().reverse());
}

function showStack(stack){
  var table = document.getElementById("stackTable");
  for(let i=4;i>=0;i--){
    if(stack[i]!==undefined){
      table.rows[i].cells[0].textContent= stack[i];
    }
  }
}

function createStackTable(){
  var table = document.getElementById("stackTable");
  for(let i=0;i<5;i++){
    var row = table.insertRow(0);
    var cell= row.insertCell(0);
  }
}

function createPins(simulator){
  var table = document.getElementById("pinsTable");
  for(let i=simulator.pins.length -1;i>=0;i--){
    var row = table.insertRow(0);
    var cell1= row.insertCell(0);
    var cell2= row.insertCell(1);
    cell1.textContent = 'Pin ' + i;
    cell2.textContent = simulator.pins[i];
  }
}

function updatePins(simulator){
  var table = document.getElementById("pinsTable");
  for(let i=simulator.pins.length -1;i>=0;i--){
    table.rows[i].cells[1].textContent= simulator.pins[i];
  }
}

function showPc(simulator){
  var s = document.getElementById("pc");
  s.textContent="PC: "+simulator.pc;
}

function displaySpeed(value){
  var sliderValue = document.getElementById("speed");
  sliderValue.textContent = value;
}

function drag(objectToMove){
  objectToMove.addEventListener("mousedown", function(event){
    dragElement(objectToMove);
  });
}

function dragElement(elmnt){

  elmnt.onmousedown = dragMouseDown; 
  var offset;

  function dragMouseDown(e){
    document.onmouseup  = closeDragElement;
    document.onmousemove  = elementDrag;
  }

  function elementDrag(e){
    var coord = getMousePosition(e);
    // set the new position
    elmnt.setAttribute("cx",coord.x);
    elmnt.setAttribute("cy", coord.y);
    elmnt.setAttribute("x",coord.x);
    elmnt.setAttribute("y", coord.y);
  }

  function closeDragElement(){
    document.onmouseup = null;
    document.onmousemove = null;
  }

  function getMousePosition(evt) {
    //returns an array that transforms the coordinate system of the current element
    //to svg window coordinates 
    var CTM = svg.getScreenCTM();
    return {
      x: (evt.clientX - CTM.e) / CTM.a,
      y: (evt.clientY - CTM.f) / CTM.d
    };
  }
}