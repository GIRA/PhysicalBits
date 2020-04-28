// Uzi.state.program.current.compiled

//buttons execute, stop, next instruction
//expand primitives switch
//

function ctorSimulator() {
  let simulator = {
     pins: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
     globals: {},
     scripts: {},
     startDate: new Date(),

     stack: [],
     callStack:[],
     pc:0,
     locals:{},
     currentScript:null,


     execute: execute,
     loadCurrentProgram: () => loadProgram(Uzi.state.program.current.compiled),
     getProgram: () => Uzi.state.program.current.compiled,
     update: updateProgram,
     start: startProgram,
     stop: stopProgram
     };
     

  //simulator.pins.forEach((item) => console.log(item));
  let interval = null;

  function getRandomInt(min, max){
    min = Math.trunc(min);
    max = Math.trunc(max); //TO DO
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }
  function updateProgram() {
    simulator.loadCurrentProgram();
  }
  function startProgram(){
    if (interval) return;
    interval = setInterval(()=>executeProgram(), 1);
  }

  function executeProgram(){
    if (simulator.currentScript.ticking) {
      if (simulator.currentScript.nextRun < millis()) {
          simulator.execute();
      }

    }

  }

  function stopProgram(){
    if (!interval) return;
    clearInterval(interval);
    interval = null;
  }

  function loadProgram(program) {
    simulator.pc = 0;
    simulator.pins=[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    simulator.stack = [];
    simulator.callStack=[];
    simulator.locals={};
    loadGlobals(program);
    loadScripts(program);
    simulator.startDate = new Date();
    simulator.currentScript = program.scripts[0];
  }

   function loadGlobals(program){
    simulator.globals ={};
    program.variables.forEach(
      (v)=>{if(v.name!=null){
        simulator.globals[v.name] = v.value;
      }}
    );
  }

  function loadScripts(program){
    simulator.scripts = {};
    program.scripts.forEach(
      (script)=>{
        simulator.scripts[script.name] =script;
        script.nextRun = 0;
        //TODO: this should go into the coroutine start
        script.lastStart=millis();
      }
    );
  }

  function writeConsole(pin) {
    console.log(simulator.pins[pin]);
  }

  function changeRandomPinValue() {
    let r = getRandomInt(0,10);
    simulator.pins[r] = Math.random();
    console.log("Pin" + r + " = " + simulator.pins[r])
  }
  function doReturn(){
      if(simulator.callStack.length==0){
        //TODO: Coroutine change
          simulator.pc = 0;
          simulator.currentScript.nextRun = simulator.currentScript.lastStart + simulator.currentScript.delay.value;
          simulator.currentScript.lastStart=millis();
        }else{
          let frame= simulator.callStack.pop();
          simulator.currentScript=frame.returnScript;
          simulator.locals=frame.returnLocals;
          simulator.pc=frame.returnPC;
          push(frame.returnValue);
        }
  }
  function next() {
    if(simulator.pc>= simulator.currentScript.instructions.length)
    {
      doReturn();
      return next();
    }
    var result = simulator.currentScript.instructions[(simulator.pc++)];
    return result;
  }

  function getPinValue(pinIndex){
    if (simulator.pins[pinIndex] > 1) {
      return 1;
    }
    else if (simulator.pins[pinIndex] < 0){
      return 0;
    }
    else
      return simulator.pins[pinIndex];
  }
  function setPinValue(pin, value){
    if (value > 1)
      value = 1;
    if (value < 0) {
      value = 0;
    }
    simulator.pins[pin] = value;
  }

  function getGlobalValue(global)
  {
    return simulator.globals[global];
  }
  function setGlobalValue(global, value)
  {
    simulator.globals[global] = value;
  }
  function getLocalValue(local)
  {
    return simulator.locals[local];
  }
  function setLocalValue(local, value)
  {
    simulator.locals[local] = value;
  }
  function push(value)
  {
    simulator.stack.push(value);
  }
  function pop(){
    if(simulator.stack.length==0)
    {
      throw "Stack Underflow";
    }
    return simulator.stack.pop();
  }

  function execute() {
    let instruction = next();
    if(instruction == undefined) {
      throw "undefined found as instruction" ;
      simulator.pc=0;
    }
    let argument = instruction.argument;

    switch (instruction.__class__) {
      case "UziPushInstruction": {
        if(instruction.argument.name==null){
          push(instruction.argument.value);
        }else{
          push(getGlobalValue(instruction.argument.name));
        }
      } break;
      case "UziScriptCallInstruction":{
        simulator.callStack.push({
          returnScript:simulator.currentScript,
          returnPC:simulator.pc,
          returnLocals:simulator.locals,
          returnValue:0
        });
        simulator.currentScript = simulator.scripts[argument];
        simulator.pc=0;
        simulator.locals ={};
        simulator.currentScript.arguments.slice().reverse().forEach((arg) => {
            simulator.locals[arg.name] = pop();
        });

      }break;
      case "UziPrimitiveCallInstruction": {
        executePrimitive(argument);
      } break;
      case "UziPopInstruction":{
        let g = pop();
        setGlobalValue(instruction.argument.name,g);
      } break;
      /////////////////
      /*case 'turn_on':
        simulator.pins[instruction.argument] = 1;
        break;
      case 'turn_off':
        simulator.pins[instruction.argument] = 0;
        break;*/
      case'write_pin':
        simulator.pins[instruction.argument] = pop();
        break;
      case'read_pin':
        if (instruction.argument < 0 ) {
          instruction.argument = 0;
        } else if (instruction.argument > 1) {
          instruction.argument = 1;
        }
        push(instruction.argument);
        break;
      case'read_global':
        push(getGlobalValue(instruction.argument));
        break;
      case'write_global':
        setGlobalValue(instruction.argument, pop());
        break;
      case'script_start':
        throw "TO DO";
        break;
      case'script_resume':
        throw "TO DO";
        break;
      case'script_stop':
        throw "TO DO";
        break;
      case'script_pause':
        throw "TO DO";
        break;
      case'UziJMPInstruction':
        simulator.pc += instruction.argument;
        break;
      case'UziJZInstruction':
        if (pop() == 0) {
            simulator.pc += instruction.argument;
        }
        break;
      case'jnz':
        if (pop() != 0) {
          simulator.pc += instruction.argument;
        }
        break;
      case 'jne': {
          let a = pop();
          let b = pop();
          if (a != b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jlt': {
          let a = pop();
          let b = pop();
          if (a < b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jlte': {
          let a = pop();
          let b = pop();
          if (a <= b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jgt': {
          let a = pop();
          let b = pop();
          if (a > b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jgte': {
          let a = pop();
          let b = pop();
          if (a >= b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'UziReadLocalInstruction': {
        push(getLocalValue(argument.name));
        }
        break;
      case 'write_local': {
          throw "TO DO";
        }
        break;
      case 'prim_read_pin': {
          /*let pin = pop();
          push(pin);*/
          throw "TO DO";
        }
        break;
      case 'prim_write_pin': {
          /*let pin = pop();
          let value = pop();
          simulator.pins[pin] =value;*/
          throw "TO DO";
        }
        break;
      case 'prim_toggle_pin': {
          throw "TO DO";
        }
        break;

      default:
        throw "Missing instruction "+ instruction.__class__;
    }
  }

  function millis(){
      let d = new Date();
      return d - simulator.startDate;
  }

  function doYield(){

  }


  function executePrimitive(prim) {
    switch (prim.name) {
      case "read": {
        let pin = pop();
        push(getPinValue(pin));
      }break;
      case "write": {
        let value = pop();
        let pin = pop();
        setPinValue(pin,value);
      }break;
      case "toggle": {
        let pin = pop();
        setPinValue(pin, 1 - getPinValue(pin));
      }break;
      case "getservodegrees": {
        //TO DO
      }break;
      case "setservodegrees": {
        //TO DO
      }break;
      case "servowrite": {
        //TO DO
      }break;
      case "multiply": {
        let val2 = pop();
        let val1 = pop();
        push(val1 * val2);
      }break;
      case "add": {
        let val2 = pop();
        let val1 = pop();
        push(val1 + val2);
      }break;
      case "divide": {
        let val2 = pop();
        let val1 = pop();
        push(val1 / val2);
      }break;
      case "subtract": {
        let val2 = pop();
        let val1 = pop();
        push(val1 - val2);
      }break;
      case "seconds": {
        push(millis() / 1000);
      }break;
      case "minutes": {
        push(millis() / 1000 / 60);
      }break;
      case "eq": {
        let val2 = pop();
        let val1 = pop();
        push(val1 == val2);
      }break;
      case "neq": {
        let val2 = pop();
        let val1 = pop();
        push(val1 != val2);
      }break;
      case "greaterThan": {
        let val2 = pop();
        let val1 = pop();
        push(val1 > val2);
      }break;
      case "gteq": {
        let val2 = pop();
        let val1 = pop();
        push(val1 >= val2);
      }break;
      case "lt": {
        let val2 = pop();
        let val1 = pop();
        push(val1 < val2);
      }break;
      case "lteq": {
        let val2 = pop();
        let val1 = pop();
        push(val1 <= val2);
      }break;
      case "negate": {
        let val = pop();
        push(val == 0 ? 1 : 0);
      }break;
      case "sin": {
          let val = pop();
          push(Math.sin(val));
      }break;
      case "cos": {
        let val = pop();
        push(Math.cos(val));
      }break;
      case "tan": {
        let val = pop();
        push(Math.tan(val));
      }break;
      case "turnOn": {
        //simulator.pins[pop()] = 1;
        setPinValue(pop(),1);
      } break;
      case "turnOff": {
        //simulator.pins[pop()] = 0;
        setPinValue(pop(),0);
      } break;
      case "yield": {
        //TO DO
      }break;
      case "delayMs": {
        let time = pop();
        simulator.currentScript.nextRun = millis() + time;
        doYield();
      }break;
      case "delayS": {
        let time = pop();
        time = time * 1000;
        simulator.currentScript.nextRun = millis() + time;
        doYield();
      }break;
      case "delayM": {
        let time = pop();
        time = time * 1000;
        time = time * 60;
        simulator.currentScript.nextRun = millis() + time;
        doYield();
      }break;
      case "millis": {
        push(millis());
      }break;
      case "ret": {
        doReturn();
      }break;
      case "pop": {
        pop();
      }break;
      case "retv": {
        let value = pop();
        let frame = simulator.callStack.pop();
        frame.returnValue=value;
        simulator.callStack.push(frame);
        doReturn();
      }break;
      case "coroutine": {
        //TO DO
      }break;
      case "logicaland": {
        let val2 = pop();
        let val1 = pop();
        push(val1 && val2);
      }break;
      case "logicalor": {
        let val2 = pop();
        let val1 = pop();
        push(val1 || val2);
      }break;
      case "bitwiseand": {
        let val2 = pop();
        let val1 = pop();
        push(val1 & val2);
      }break;
      case "bitwiseor": {
        let val2 = pop();
        let val1 = pop();
        push(val1 | val2);
      }break;
      case "serialwrite": {
        //TO DO
      }break;
      case "round": {
        let a = pop();
        push(Math.round(a));
      }break;
      case "ceil": {
        let a = pop();
        push(Math.ceil(a));
      }break;
      case "floor": {
        let a = pop();
        push(Math.floor(a));
      }break;
      case "sqrt": {
        let a = pop();
        push(Math.sqrt(a));
      }break;
      case "abs": {
        let a = pop();
        push(Math.abs(a));
      }break;
      case "ln": {
        let a = pop();
        push(Math.log(a));
      }break;
      case "log10": {
        let a = pop();
        push(Math.log10(a));
      }break;
      case "exp": {
        let a = pop();
        push(Math.exp(a));
      }break;
      case "pow": {
        let a = pop();
        push(Math.pow(10,a));
      }break;
      case "asin": {
        let a = pop();
        push(Math.asin(a));
      }break;
      case "acos": {
        let a = pop();
        push(Math.acos(a));
      }break;
      case "atan": {
        let a = pop();
        push(Math.atan(a));
      }break;
      case "atan2": {
        let x = pop();
        let y = pop();
        push(Math.atan2(y,x));
      }break;
      case "power": {
        let b = pop();
        let a = pop();
        push(Math.pow(a,b));
      }break;
      case "isOn": {
        let pin = pop();
        push(getPinValue(pin) > 0);
      }break;
      case "isOff": {
        let pin = pop();
        push(getPinValue(pin) == 0);
      }break;
      case "remainder": {
        let b = pop();
        let a = pop();
        push(a % b);
      }break;
      case "mod": {
        let n = pop();
        let a = pop();
        push(a - (Math.floor(a/n)*n));
      }break;
      case "constrain": {
        let c = pop();
        let b = pop();
        let a = pop();
        if (a < b) {
          push(b);
        } else if (a > c) {
          push(c);
        } else{
          push(a);
        }
      }break;
      case "randomint": {
        let b = pop();
        let a = pop();
        if (b > a ) {
          push(getRandomInt(a,b));
        }
        else {
          push(getRandomInt(b,a));
        }
        getRandomInt()
      }break;
      case "isEven":{
          push(pop()%2==0);
      }break;
      default:
        throw "Missing primitive "+ prim.name;
    }
  }

  //setInterval(changeRandomPinValue, 1000);
  return simulator;
};

if(typeof module != "undefined")
     {
       module.exports = ctorSimulator;
     }
