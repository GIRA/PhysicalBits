// Uzi.state.program.compiled

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
     this.currentProgram = null;
     this.yieldFlag = false;

     this.interval = null;
     this.millisMock = null;
     this.lastTickStart = null;

     this.expectedBkp = null;
   };

  updateProgram() {
    this.loadProgram(Uzi.state.program.compiled);
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
    this.currentProgram = program;
  }

  loadGlobals(program){
    this.globals ={};
    program.globals.forEach((v) => {
      if(v.name!=null){
        this.globals[v.name] = v.value;
      }
    });
  }

  loadScripts(program){
    this.scripts = {};
    program.scripts.forEach((script)=>{
      this.scripts[script.name] = script;
      script.nextRun = 0;
      script.ticking = script["running?"];
      //TODO: this should go into the coroutine start
      script.lastStart = this.millis();
    });
  }

  millis(){
    if(this.millisMock == undefined){
      let d = new Date();
      return d - this.startDate;
    }else{
      return this.millisMock;
    }
  }

  startProgram(speed){
    if (this.interval) return;
    this.interval = setInterval(()=>this.executeProgram(), speed);
  }

  executeProgram(){
    this.lastTickStart = this.millis();
    this.currentProgram.scripts.forEach((script) => {
    if (script.ticking) {
      if (script.nextRun <= this.lastTickStart) {
          this.executeScript(script);
      }
    }
    });
  }

  executeScript(script){
    this.contextSwitch(script);
    while(true){
      if(this.pc <= this.getInstructionStop()){
        let instruction = this.getInstructionAt(this.pc);
        if(instruction.breakpoint && instruction.breakpoint == this.expectedBkp){

          throw {
            instruction: instruction
          };
        }
        this.pc++;
        this.executeInstruction(instruction);
      }
      //TODO(Nico): Handle stack error
      if(this.yieldFlag){
        this.yieldFlag = false;
        break;
      }
      if(this.pc > this.getInstructionStop()){
        this.pc = this.getInstructionStart();
        this.doReturn();
        break;
      }
    }
  }

  contextSwitch(newScript){
    if(this.currentScript != newScript){
      if(this.currentScript){
        let vmState= {
          pc : this.pc,
          locals : this.locals,
          //TODO: callStack : this.callStack,
          stack :this.stack
        };
       this.currentScript.vmState = JSONX.stringify(vmState);
      }
      this.currentScript = newScript;
      if(newScript.vmState)
      {
        let vmState = JSONX.parse(newScript.vmState);
        this.pc = vmState.pc;
        this.locals = vmState.locals;
        this.stack = vmState.stack;
      }else{
        this.pc = this.getInstructionStart();
        this.currentScript.lastStart = this.lastTickStart;
      }
    }
  }

  executeInstruction(instruction) {
    if(instruction == undefined)
    {
      debugger;
    }
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

  doReturn(){
    if(this.callStack.length==0){
      //TODO: Coroutine change
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

  executeUntilBreakPoint(bkp, safeguard){ //TODO: it may not work with yield in the middle of the scripts. Also delete safeguard?
    this.expectedBkp = bkp;
    try {
      this.executeProgram();
    } catch (error) {
      if(error.instruction.breakpoint == bkp){
        return;
      }else{
        throw error;
      }
    }
  }

  getInstructionStop(){
    let ac = 0;
    for(let i = 0; i < this.currentProgram.scripts.length; i++){
      ac += this.currentProgram.scripts[i].instructions.length;
      if(this.currentProgram.scripts[i] === this.currentScript){
        break;
      }
    }
    return ac - 1;
  }

  getInstructionStart(){
    let ac = 0;
    for(let i = 0; i < this.currentProgram.scripts.length; i++){
      if(this.currentProgram.scripts[i] === this.currentScript){
        break;
      }
      ac += this.currentProgram.scripts[i].instructions.length;
    }
    return ac;
  }

  getInstructionAt(pc){
    let ac = 0;
    for(let i = 0; i < this.currentProgram.scripts.length; i++){
      if(this.currentProgram.scripts[i] === this.currentScript){
        return this.currentScript.instructions[pc - ac];
      }
      ac += this.currentProgram.scripts[i].instructions.length;
    }
  }

  stopProgram(){
    if (!this.interval) return;
    clearInterval(this.interval);
    this.interval = null;
  }

  getRandomInt(min, max){
    min = Math.trunc(min);
    max = Math.trunc(max); //TO DO
    return Math.floor(Math.random() * (max - min + 1)) + min;
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

  incrementMillisAndExecute(increment){
    let millis = this.millis();
    for(let i = 0; i < increment; i++){
      this.setMillis(this.millis + i);
      this.executeProgram();
    }
  }

  setMillis(millis){
    this.millisMock = millis;
  }

  doYield(){

  }

};

if(typeof module != "undefined")
     {
      module.exports = Simulator;
     }
