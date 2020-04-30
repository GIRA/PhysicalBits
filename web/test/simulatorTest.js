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

  it('set/get pin value', function () {
    sim.setPinValue(6, 1);
    assert.equal(1, sim.getPinValue(6));
  });

  it('turn off pin', function() {
    sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":0},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":6}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":21,"name":"turnOff","stackTransition":{"__class__":"Association","key":1,"value":0}}},{"__class__":"UziStopScriptInstruction","argument":"test"}],"locals":[],"name":"test","ticking":true}],"variables":[{"__class__":"UziVariable","name":null,"value":0},{"__class__":"UziVariable","name":null,"value":6}]});
    sim.setPinValue(6, 1);
    for (let i = 0; i < 2; i++) {
      sim.execute();
    }
    assert.equal(0, sim.getPinValue(6));
  });
});
//   sim.loadProgram({"__class__":"UziProgram","scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","name":null,"value":0},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":null,"value":6}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","code":21,"name":"turnOff","stackTransition":{"__class__":"Association","key":1,"value":0}}},{"__class__":"UziStopScriptInstruction","argument":"test"}],"locals":[],"name":"test","ticking":true}],"variables":[{"__class__":"UziVariable","name":null,"value":0},{"__class__":"UziVariable","name":null,"value":6}]});
