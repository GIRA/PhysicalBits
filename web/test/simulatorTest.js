var assert = require('assert');
var simCtor = require('../ide/simulator');

function initializeSimulator()
{
    return simCtor();
}

it('sanity-check', function () {
  assert.equal(0, 0);
});

describe('Simulator Tests', function () {
  let sim = null;

  beforeEach(function () {
    sim = initializeSimulator();
  });

  afterEach(function () {
    sim.stop();
    sim = null;
  });

  function loop(loops)
  {
  if(typeof loops === 'number')
  {
    for(let i = 0; i < loops; i++){
      sim.execute();
    }
  }else {
    throw 'parameter of loop function is not a number';
  }
  }

  it('set/get pin value', function () {
    sim.setPinValue(6, 1);
    assert.equal(1, sim.getPinValue(6));
  });

  it('turn off pin', function() {
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":0},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":6}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":21,"name":"turnOff","stackTransition":{"__class__":"Association","key":1,"value":0}}},{"__class__":"UziStopScriptInstruction","argument":"test"}],"locals":[],"name":"test","ticking":true}],"variables":[{"__class__":"UziVariable","name":null,"value":0},{"__class__":"UziVariable","name":null,"value":6}]});
    sim.setPinValue(6, 1);
    loop(2);
    assert.equal(0, sim.getPinValue(6));
  });

  it('turn on pin', function() {
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":6}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true,"nextRun":336503,"lastStart":406}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":6}]})
     sim.setPinValue(6,0);
     loop(2);
     assert.equal(1,sim.getPinValue(6));
  });

  it('toggle pin', function() {
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":6}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":2,"name":"toggle","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true,"nextRun":61384,"lastStart":12}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":6}]});
     sim.setPinValue(6,0);
     loop(2);
     assert.equal(1, sim.getPinValue(6));
     loop(2);
     assert.equal(0, sim.getPinValue(6));
  });

  it('multiplication', function(){ // 2 * 3
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":2}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":3}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":5,"name":"multiply","stackTransition":{"__class__":"Association","key":2,"value":1}}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true,"nextRun":10744,"lastStart":18}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":2},{"__class__":"UziVariable","name":null,"value":3}]});
    loop(3);
    assert.equal(6, sim.stack[0]);
  });

  it('division', function(){ /* 12 / 4 */
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":12}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":4}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":7,"name":"divide","stackTransition":{"__class__":"Association","key":2,"value":1}}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":12},{"__class__":"UziVariable","name":null,"value":4}]});
    loop(3);
    assert.equal(3, sim.stack[0]);
  });

  it('addition', function(){ // 3 + 4
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":3}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":4}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":6,"name":"add","stackTransition":{"__class__":"Association","key":2,"value":1}}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true,"nextRun":58155,"lastStart":19}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":3},{"__class__":"UziVariable","name":null,"value":4}]});
    loop(3);
    assert.equal(7, sim.stack[0]);
  });

  it('subtraction', function(){ // 8 - 4
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":8}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":4}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":8,"name":"subtract","stackTransition":{"__class__":"Association","key":2,"value":1}}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":8},{"__class__":"UziVariable","name":null,"value":4}]});
    loop(3);
    assert.equal(4, sim.stack[0]);
  });

  it('power', function(){
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":3}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":2}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":46,"name":"power","stackTransition":{"__class__":"Association","key":2,"value":1}}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true,"nextRun":483389,"lastStart":21}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":3},{"__class__":"UziVariable","name":null,"value":2}]});
    loop(3);
    assert.equal(9, sim.stack[0]);
  });

  it('sqrt', function(){
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":9}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":37,"name":"sqrt","stackTransition":{"__class__":"Association","key":1,"value":1}}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":9}]});
    loop(2);
    assert.equal(3, sim.stack[0]);
  })

  it('isOn / isOff', function() { //turned on D4
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":4}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true,"nextRun":355810,"lastStart":10}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":4}]});
    loop(2);
    assert.equal(true, sim.getPinValue(4) > 0);
    sim.setPinValue(4, 0);
    assert.equal(true, sim.getPinValue(4) == 0);
  })
  /*it('eq', function(){ //TODO: Missing primitive Equals
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":1000},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":1}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":1}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":10,"name":"equals","stackTransition":{"__class__":"Association","key":2,"value":1}}},{"__class__":"UziJZInstruction","argument":2},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":6}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":20,"name":"turnOn","stackTransition":{"__class__":"Association","key":1,"value":0}}}],"locals":[],"name":"default","ticking":true,"nextRun":1068002,"lastStart":27}],"variables":[{"__class__":"UziVariable","name":null,"value":1000},{"__class__":"UziVariable","name":null,"value":1},{"__class__":"UziVariable","name":null,"value":6}]});
    loop(3);
    assert.equal(1,1);
  })*/


});
