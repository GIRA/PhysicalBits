function ctorGraphics()
{
  let graphics = {
    drawCircles: drawCircles,
    createPins: createPins,
    showStats: showStats,
    createStackTable: createStackTable
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
    c.addEventListener("mousedown", function(){
      if(simulator.pins[i]==0){
        simulator.pins[i]=1;
      } else{
        simulator.pins[i]=0;
      }
    });
    setInterval(() => {
       if(simulator.pins[i]>=0.5) {
           c.setAttribute("fill", "chartreuse");
       } else{
          c.setAttribute("fill", "black");
       }
    }, 1);
  }
}

function showStats(sim){
  setInterval(() => {
    showGlobals(sim);
    updateStack(sim);
    updatePins(sim)
    showPc(sim)
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
  s.innerHTML="PC: "+simulator.pc;
}

