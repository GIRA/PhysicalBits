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
    interval = setInterval(()=>simulator.execute(), 500);
  }
  function stopProgram(){
    if (!interval) return;
    clearInterval(interval);
    interval = null;
  }

  function loadProgram(program) {
    simulator.pc = 0;
    simulator.stack = [];
    simulator.callStack=[];
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
      (v)=>{
        simulator.scripts[v.name] = v;
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

  function next() {
    if(simulator.pc>= simulator.currentScript.instructions.length)
    {
        if(  simulator.callStack.length==0){
            simulator.pc = 0;
          }else{
            let frame= simulator.callStack.pop();
            simulator.currentScript=frame.returnScript;
            simulator.pc=frame.returnPC;
            return next();
          }
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
          simulator.stack.push(instruction.argument.value);
        }else{
          simulator.stack.push(getGlobalValue(instruction.argument.name));
        }
      } break;
      case "UziScriptCallInstruction":{
        simulator.callStack.push({
          returnScript:simulator.currentScript,
          returnPC:simulator.pc+1
        });
        simulator.currentScript=simulator.scripts[argument];
        simulator.pc=0;
      }break;
      case "UziPrimitiveCallInstruction": {
        executePrimitive(argument);
      } break;
      case "UziPopInstruction":{
        let g = simulator.stack.pop();
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
        simulator.pins[instruction.argument] = simulator.stack.pop();
        break;
      case'read_pin':
        if (instruction.argument < 0 ) {
          instruction.argument = 0;
        } else if (instruction.argument > 1) {
          instruction.argument = 1;
        }
        simulator.stack.push(instruction.argument);
        break;
      case'read_global':
        simulator.stack.push(getGlobalValue(instruction.argument));
        break;
      case'write_global':
        setGlobalValue(instruction.argument, simulator.stack.pop());
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
      case'jmp':
        simulator.pc += instruction.argument;
        break;
      case'jz':
        if (simulator.stack.pop() == 0) {
            simulator.pc += instruction.argument;
        }
        break;
      case'jnz':
        if (simulator.stack.pop() != 0) {
          simulator.pc += instruction.argument;
        }
        break;
      case 'jne': {
          let a = simulator.stack.pop();
          let b = simulator.stack.pop();
          if (a != b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jlt': {
          let a = simulator.stack.pop();
          let b = simulator.stack.pop();
          if (a < b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jlte': {
          let a = simulator.stack.pop();
          let b = simulator.stack.pop();
          if (a <= b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jgt': {
          let a = simulator.stack.pop();
          let b = simulator.stack.pop();
          if (a > b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'jgte': {
          let a = simulator.stack.pop();
          let b = simulator.stack.pop();
          if (a >= b) {
            simulator.pc += instruction.argument;
          }
        }
        break;
      case 'read_local': {
          throw "TO DO";
        }
        break;
      case 'write_local': {
          throw "TO DO";
        }
        break;
      case 'prim_read_pin': {
          /*let pin = simulator.stack.pop();
          simulator.stack.push(pin);*/
          throw "TO DO";
        }
        break;
      case 'prim_write_pin': {
          /*let pin = simulator.stack.pop();
          let value = simulator.stack.pop();
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

  function millis()
  {
      let d = new Date();
      return d - simulator.startDate;
  }



  function executePrimitive(prim) {
    switch (prim.name) {
      case "read": {
        let pin = simulator.stack.pop();
        simulator.stack.push(getPinValue(pin));
      }break;
      case "write": {
        let value = simulator.stack.pop();
        let pin = simulator.stack.pop();
        setPinValue(pin,value);
      }break;
      case "toggle": {
        let pin = simulator.stack.pop();
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
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 * val2);
      }break;
      case "add": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 + val2);
      }break;
      case "divide": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 / val2);
      }break;
      case "subtract": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 - val2);
      }break;
      case "seconds": {
        simulator.stack.push(millis() / 1000);
      }break;
      case "minutes": {
        simulator.stack.push(millis() / 1000 / 60);
      }break;
      case "eq": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 == val2);
      }break;
      case "neq": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 != val2);
      }break;
      case "gt": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 > val2);
      }break;
      case "gteq": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 >= val2);
      }break;
      case "lt": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 < val2);
      }break;
      case "lteq": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 <= val2);
      }break;
      case "negate": {
        let val = simulator.stack.pop();
        simulator.stack.push(val == 0 ? 1 : 0);
      }break;
      case "sin": {
          let val = simulator.stack.pop();
          simulator.stack.push(Math.sin(val));
      }break;
      case "cos": {
        let val = simulator.stack.pop();
        simulator.stack.push(Math.cos(val));
      }break;
      case "tan": {
        let val = simulator.stack.pop();
        simulator.stack.push(Math.tan(val));
      }break;
      case "turnOn": {
        //simulator.pins[simulator.stack.pop()] = 1;
        setPinValue(simulator.stack.pop(),1);
      } break;
      case "turnOff": {
        //simulator.pins[simulator.stack.pop()] = 0;
        setPinValue(simulator.stack.pop(),0);
      } break;
      case "yield": {
        //TO DO
      }break;
      case "delaymilis": {
        //TO DO
      }break;
      case "delayseconds": {
        //TO DO
      }break;
      case "delayminutes": {
        //TO DO
      }break;
      case "millis": {
        simulator.stack.push(millis());
      }break;
      case "ret": {
        //TO DO
      }break;
      case "pop": {
        simulator.stack.pop();
      }break;
      case "retv": {
        //TO DO
      }break;
      case "coroutine": {
        //TO DO
      }break;
      case "logicaland": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 && val2);
      }break;
      case "logicalor": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 || val2);
      }break;
      case "bitwiseand": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 & val2);
      }break;
      case "bitwiseor": {
        let val2 = simulator.stack.pop();
        let val1 = simulator.stack.pop();
        simulator.stack.push(val1 | val2);
      }break;
      case "serialwrite": {
        //TO DO
      }break;
      case "round": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.round(a));
      }break;
      case "ceil": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.ceil(a));
      }break;
      case "floor": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.floor(a));
      }break;
      case "sqrt": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.sqrt(a));
      }break;
      case "abs": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.abs(a));
      }break;
      case "ln": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.log(a));
      }break;
      case "log10": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.log10(a));
      }break;
      case "exp": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.exp(a));
      }break;
      case "pow": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.pow(10,a));
      }break;
      case "asin": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.asin(a));
      }break;
      case "acos": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.acos(a));
      }break;
      case "atan": {
        let a = simulator.stack.pop();
        simulator.stack.push(Math.atan(a));
      }break;
      case "atan2": {
        let x = simulator.stack.pop();
        let y = simulator.stack.pop();
        simulator.stack.push(Math.atan2(y,x));
      }break;
      case "power": {
        let b = simulator.stack.pop();
        let a = simulator.stack.pop();
        simulator.stack.push(Math.pow(a,b));
      }break;
      case "isOn": {
        let pin = simulator.stack.pop();
        simulator.stack.push(getPinValue(pin) > 0);
      }break;
      case "isOff": {
        let pin = simulator.stack.pop();
        simulator.stack.push(getPinValue(pin) == 0);
      }break;
      case "remainder": {
        let b = simulator.stack.pop();
        let a = simulator.stack.pop();
        simulator.stack.push(a % b);
      }break;
      case "mod": {
        let n = simulator.stack.pop();
        let a = simulator.stack.pop();
        simulator.stack.push(a - (Math.floor(a/n)*n));
      }break;
      case "constrain": {
        let c = simulator.stack.pop();
        let b = simulator.stack.pop();
        let a = simulator.stack.pop();
        if (a < b) {
          simulator.stack.push(b);
        } else if (a > c) {
          simulator.stack.push(c);
        } else{
          simulator.stack.push(a);
        }
      }break;
      case "randomint": {
        let b = simulator.stack.pop();
        let a = simulator.stack.pop();
        if (b > a ) {
          simulator.stack.push(getRandomInt(a,b));
        }
        else {
          simulator.stack.push(getRandomInt(b,a));
        }
        getRandomInt()
      }break;
      default:
        throw "Missing primitive "+ prim.name;
    }
  }

  //setInterval(changeRandomPinValue, 1000);
  return simulator;
};
