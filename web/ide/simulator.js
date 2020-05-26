// Uzi.state.program.current.compiled

//buttons execute, stop, next instruction
//expand primitives switch
//

class Simulator {
  constructor(){
     this.pins =  [0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
     this.globals = {};
     this.scripts = {};
     this.startDate = new Date();

     this.stack = [];
     this.callStack = [];
     this.pc = 0;
     this.locals = {};
     this.currentScript = null;

     this.interval = null;
   };
   

  //simulator.pins.forEach((item) => console.log(item));
  

  getRandomInt(min, max){
    min = Math.trunc(min);
    max = Math.trunc(max); //TO DO
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }
  updateProgram() {
    this.getProgram();
  }
  startProgram(speed){
    if (this.interval) return;
    this.interval = setInterval(()=>this.executeProgram(), speed);
  }

  executeProgram(){
    if (this.currentScript.ticking) {
      if (this.currentScript.nextRun < this.millis()) {
          this.execute();
      }
    }
  }

  executeUntilBreakPoint(bkp, safeguard){
    // TODO(Richo): El safeguard podría ser un parámetro optativo --> done
    // TODO(Richo): Si sale por safeguard que levante una excepción así el caller se entera --> Done
    if(this.currentScript.ticking){
      if(true || this.currentScript.nextRun < this.millis()){
        let next;
        do {
          safeguard--;

          next = this.currentScript.instructions[this.pc];
          if (next.breakpoint == bkp) {
            break;
          } else {
            this.executeInstruction(next);
            this.pc++;
          }

        } while (this.pc < this.currentScript.instructions.length && safeguard > 0);

        if(safeguard <= 0){
          throw 'Safeguard exception: the program ran out of cycles. Stopped running to avoid an infinite loop';
        }
      }
    }
  }

  getProgram(){
    return this.loadProgram(Uzi.state.program.current.compiled);
  }

  stopProgram(){
    if (!this.interval) return;
    clearInterval(this.interval);
    this.interval = null;
  }

  loadProgram(program) {
    this.pc = 0;
    this.pins=[0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    this.stack = [];
    this.callStack=[];
    this.locals={};
    this.loadGlobals(program);
    this.loadScripts(program);
    this.startDate = new Date();
    this.currentScript = program.scripts[0];     
  }

  loadGlobals(program){
    this.globals ={};
    program.variables.forEach(
      (v)=>{if(v.name!=null){
        this.globals[v.name] = v.value;
      }}
    );
  }

  loadScripts(program){
    this.scripts = {};
    program.scripts.forEach(
      (script)=>{
        this.scripts[script.name] =script;
        script.nextRun = 0;
        //TODO: this should go into the coroutine start
        script.lastStart = this.millis();
      }
    );
  }

  writeConsole(pin) {
    console.log(this.pins[pin]);
  }

  changeRandomPinValue() {
    let r = getRandomInt(0,10);
    this.pins[r] = Math.random();
    console.log("Pin" + r + " = " + this.pins[r])
  }
  doReturn(){
      if(this.callStack.length==0){
        //TODO: Coroutine change
          this.pc = 0;
          this.currentScript.nextRun = this.currentScript.lastStart + this.currentScript.delay.value;
          this.currentScript.lastStart = this.millis();
        }else{
          let frame= this.callStack.pop();
          this.currentScript=frame.returnScript;
          this.locals=frame.returnLocals;
          this.pc=frame.returnPC;
          this.push(frame.returnValue);
        }
  }
  next() {
    if(this.pc>= this.currentScript.instructions.length)
    {
      this.doReturn();
      return this.next();
    }
    var result = this.currentScript.instructions[(this.pc++)];
    return result;
  }

  getPinValue(pinIndex){
    if (this.pins[pinIndex] > 1) {
      return 1;
    }
    else if (this.pins[pinIndex] < 0){
      return 0;
    }
    else
      return this.pins[pinIndex];
  }
  setPinValue(pin, value){
    if (value > 1)
      value = 1;
    if (value < 0) {
      value = 0;
    }
    this.pins[pin] = value;
  }

  getGlobalValue(global)
  {
    return this.globals[global];
  }
  setGlobalValue(global, value)
  {
    this.globals[global] = value;
  }
  getLocalValue(local)
  {
    return this.locals[local];
  }
  setLocalValue(local, value)
  {
    this.locals[local] = value;
  }
  push(value)
  {
    this.stack.push(value);
  }
  pop(){
    if(this.stack.length==0)
    {
      throw "Stack Underflow";
    }
    return this.stack.pop();
  }

  execute() {
    let instruction = this.next();
    if(instruction == undefined) {
      throw "undefined found as instruction" ;
      this.pc=0;
    }
    this.executeInstruction(instruction);
  }

  executeInstruction(instruction) {
    let argument = instruction.argument;

    switch (instruction.__class__) {
      case "UziPushInstruction": {
        if(instruction.argument.name==null){
          this.push(instruction.argument.value);
        }else{
          this.push(this.getGlobalValue(instruction.argument.name));
        }
      } break;
      case "UziScriptCallInstruction":{
        this.callStack.push({
          returnScript:this.currentScript,
          returnPC:this.pc,
          returnLocals:this.locals,
          returnValue:0
        });
        this.currentScript = this.scripts[argument];
        this.pc=0;
        this.locals ={};
        this.currentScript.arguments.slice().reverse().forEach((arg) => {
            this.locals[arg.name] = this.pop();
        });

      }break;
      case "UziPrimitiveCallInstruction": {
        this.executePrimitive(argument);
      } break;
      case "UziPopInstruction":{
        let g = this.pop();
        this.setGlobalValue(instruction.argument.name,g);
      } break;
      case "UziStopScriptInstruction": {
        // TODO(Richo): Implement this instruction!

      } break;
      /////////////////
      /*case 'turn_on':
        this.pins[instruction.argument] = 1;
        break;
      case 'turn_off':
        this.pins[instruction.argument] = 0;
        break;*/
      case'write_pin':
        this.pins[instruction.argument] = this.pop();
        break;
      case'read_pin':
        if (instruction.argument < 0 ) {
          instruction.argument = 0;
        } else if (instruction.argument > 1) {
          instruction.argument = 1;
        }
        this.push(instruction.argument);
        break;
      case'read_global':
        this.push(this.getGlobalValue(instruction.argument));
        break;
      case'write_global':
        this.setGlobalValue(instruction.argument, this.pop());
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
        this.pc += instruction.argument;
        break;
      case'UziJZInstruction':
        if (this.pop() == 0) {
            this.pc += instruction.argument;
        }
        break;
      case'jnz':
        if (this.pop() != 0) {
          this.pc += instruction.argument;
        }
        break;
      case 'jne': {
          let a = this.pop();
          let b = this.pop();
          if (a != b) {
            this.pc += instruction.argument;
          }
        }
        break;
      case 'jlt': {
          let a = this.pop();
          let b = this.pop();
          if (a < b) {
            this.pc += instruction.argument;
          }
        }
        break;
      case 'jlte': {
          let a = this.pop();
          let b = this.pop();
          if (a <= b) {
            this.pc += instruction.argument;
          }
        }
        break;
      case 'jgt': {
          let a = this.pop();
          let b = this.pop();
          if (a > b) {
            this.pc += instruction.argument;
          }
        }
        break;
      case 'jgte': {
          let a = this.pop();
          let b = this.pop();
          if (a >= b) {
            this.pc += instruction.argument;
          }
        }
        break;
      case 'UziReadLocalInstruction': {
        this.push(this.getLocalValue(argument.name));
        }
        break;
      case 'write_local': {
          throw "TO DO";
        }
        break;
      case 'prim_read_pin': {
          /*let pin = this.pop();
          push(pin);*/
          throw "TO DO";
        }
        break;
      case 'prim_write_pin': {
          /*let pin = this.pop();
          let value = this.pop();
          this.pins[pin] =value;*/
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

  millis(){
      let d = new Date();
      return d - this.startDate;
  }

  doYield(){

  }


  executePrimitive(prim) {
    switch (prim.name) {
      case "read": {
        let pin = this.pop();
        this.push(this.getPinValue(pin));
      }break;
      case "write": {
        let value = this.pop();
        let pin = this.pop();
        this.setPinValue(pin,value);
      }break;
      case "toggle": {
        let pin = this.pop();
        this.setPinValue(pin, 1 - this.getPinValue(pin));
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
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 * val2);
      }break;
      case "add": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 + val2);
      }break;
      case "divide": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 / val2);
      }break;
      case "subtract": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 - val2);
      }break;
      case "seconds": {
        this.push(this.millis() / 1000);
      }break;
      case "minutes": {
        this.push(this.millis() / 1000 / 60);
      }break;
      case "equals": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 == val2);
      }break;
      case "notEquals": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 != val2);
      }break;
      case "greaterThan": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 > val2);
      }break;
      case "greaterThanOrEquals": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 >= val2);
      }break;
      case "lessThan": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 < val2);
      }break;
      case "lessThanOrEquals": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 <= val2);
      }break;
      case "negate": {
        let val = this.pop();
        this.push(val == 0 ? 1 : 0);
      }break;
      case "sin": {
          let val = this.pop();
          this.push(Math.sin(val));
      }break;
      case "cos": {
        let val = this.pop();
        this.push(Math.cos(val));
      }break;
      case "tan": {
        let val = this.pop();
        this.push(Math.tan(val));
      }break;
      case "turnOn": {
        //this.pins[this.pop()] = 1;
        this.setPinValue(this.pop(),1);
      } break;
      case "turnOff": {
        //this.pins[this.pop()] = 0;
        this.setPinValue(this.pop(),0);
      } break;
      case "yield": {
        //TO DO
      }break;
      case "delayMs": {
        let time = this.pop();
        this.currentScript.nextRun = this.millis() + time;
        this.doYield();
      }break;
      case "delayS": {
        let time = this.pop();
        time = time * 1000;
        this.currentScript.nextRun = this.millis() + time;
        doYield();
      }break;
      case "delayM": {
        let time = this.pop();
        time = time * 1000;
        time = time * 60;
        this.currentScript.nextRun = this.millis() + time;
        doYield();
      }break;
      case "millis": {
        this.push(this.millis());
      }break;
      case "ret": {
        this.doReturn();
      }break;
      case "pop": {
        pop();
      }break;
      case "retv": {
        let value = pop();
        let frame = this.callStack.pop();
        frame.returnValue=value;
        this.callStack.push(frame);
        doReturn();
      }break;
      case "coroutine": {
        //TO DO
      }break;
      case "logicaland": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 && val2);
      }break;
      case "logicalor": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 || val2);
      }break;
      case "bitwiseand": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 & val2);
      }break;
      case "bitwiseor": {
        let val2 = this.pop();
        let val1 = this.pop();
        this.push(val1 | val2);
      }break;
      case "serialwrite": {
        //TO DO
      }break;
      case "round": {
        let a = this.pop();
        this.push(Math.round(a));
      }break;
      case "ceil": {
        let a = this.pop();
        this.push(Math.ceil(a));
      }break;
      case "floor": {
        let a = this.pop();
        this.push(Math.floor(a));
      }break;
      case "sqrt": {
        let a = this.pop();
        this.push(Math.sqrt(a));
      }break;
      case "abs": {
        let a = this.pop();
        this.push(Math.abs(a));
      }break;
      case "ln": {
        let a = this.pop();
        this.push(Math.log(a));
      }break;
      case "log10": {
        let a = this.pop();
        this.push(Math.log10(a));
      }break;
      case "exp": {
        let a = this.pop();
        this.push(Math.exp(a));
      }break;
      case "pow10": {
        let a = this.pop();
        this.push(Math.pow(10,a));
      }break;
      case "asin": {
        let a = this.pop();
        this.push(Math.asin(a));
      }break;
      case "acos": {
        let a = this.pop();
        this.push(Math.acos(a));
      }break;
      case "atan": {
        let a = this.pop();
        this.push(Math.atan(a));
      }break;
      case "atan2": {
        let x = this.pop();
        let y = this.pop();
        this.push(Math.atan2(y,x));
      }break;
      case "power": {
        let b = this.pop();
        let a = this.pop();
        this.push(Math.pow(a,b));
      }break;
      case "isOn": {
        let pin = this.pop();
        this.push(this.getPinValue(pin) > 0);
      }break;
      case "isOff": {
        let pin = this.pop();
        this.push(this.getPinValue(pin) == 0);
      }break;
      case "remainder": {
        let b = this.pop();
        let a = this.pop();
        this.push(a % b);
      }break;
      case "mod": {
        let n = this.pop();
        let a = this.pop();
        this.push(a - (Math.floor(a/n)*n));
      }break;
      case "constrain": {
        let c = this.pop();
        let b = this.pop();
        let a = this.pop();
        if (a < b) {
          this.push(b);
        } else if (a > c) {
          this.push(c);
        } else{
          this.push(a);
        }
      }break;
      case "randomint": {
        let b = this.pop();
        let a = this.pop();
        if (b > a ) {
          this.push(getRandomInt(a,b));
        }
        else {
          this.push(getRandomInt(b,a));
        }
        getRandomInt()
      }break;
      case "isDivisibleBy": {
        //TO DO
      }break;
      case "isEven":{
          this.push(this.pop()%2==0);
      }break;
      default:
        throw "Missing primitive "+ prim.name;
    }
  }

  //setInterval(changeRandomPinValue, 1000);
  
};

let simulator = new Simulator;

if(typeof module != "undefined")
     {
       module.exports = simulator;
     }
