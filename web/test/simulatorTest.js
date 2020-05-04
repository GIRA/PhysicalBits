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

  it('turn on pin', function() {
    sim.loadProgram({
      "__class__": "UziProgram",
      "scripts": [
       {
        "__class__": "UziScript",
        "arguments": [],
        "delay": {
         "__class__": "UziVariable",
         "name": null,
         "value": 1000
        },
        "instructions": [
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 6
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 20,
           "name": "turnOn",
           "stackTransition": {
            "__class__": "Association",
            "key": 1,
            "value": 0
           }
          }
         }
        ],
        "locals": [],
        "name": "default",
        "ticking": true
       }
      ],
      "variables": [
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 1000
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 6
       }
      ]
     })
     sim.setPinValue(6,0);
     for (let i = 0; i < 2; i++) {
      sim.execute();
    }
     assert.equal(1,sim.getPinValue(6));
  });

  it('toggle pin', function() {
    sim.loadProgram({
      "__class__": "UziProgram",
      "scripts": [
       {
        "__class__": "UziScript",
        "arguments": [],
        "delay": {
         "__class__": "UziVariable",
         "name": null,
         "value": 1000
        },
        "instructions": [
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 6
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 2,
           "name": "toggle",
           "stackTransition": {
            "__class__": "Association",
            "key": 1,
            "value": 0
           }
          }
         }
        ],
        "locals": [],
        "name": "default",
        "ticking": true
       }
      ],
      "variables": [
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 1000
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 6
       }
      ]
     });
     sim.setPinValue(6,0);
     for(let i = 0; i < 2; i++)
     {
       sim.execute();
     }
     assert.equal(1, sim.getPinValue(6));
     for(let i = 0; i < 2; i++)
     {
       sim.execute();
     }
     assert.equal(0, sim.getPinValue(6));
  });

  it('multiplication', function(){ // 2 * 3
    sim.loadProgram({
      "__class__": "UziProgram",
      "scripts": [
       {
        "__class__": "UziScript",
        "arguments": [],
        "delay": {
         "__class__": "UziVariable",
         "name": null,
         "value": 1000
        },
        "instructions": [
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 2
          }
         },
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 3
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 5,
           "name": "multiply",
           "stackTransition": {
            "__class__": "Association",
            "key": 2,
            "value": 1
           }
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 20,
           "name": "turnOn",
           "stackTransition": {
            "__class__": "Association",
            "key": 1,
            "value": 0
           }
          }
         }
        ],
        "locals": [],
        "name": "default",
        "ticking": true
       }
      ],
      "variables": [
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 1000
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 2
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 3
       }
      ]
     });
     for(let i = 0; i < 3; i++){
       sim.execute();
     }
     assert.equal(6, sim.stack[0]);
  });

  it('division', function(){ /* 12 / 4 */
    sim.loadProgram({
      "__class__": "UziProgram",
      "scripts": [
       {
        "__class__": "UziScript",
        "arguments": [],
        "delay": {
         "__class__": "UziVariable",
         "name": null,
         "value": 1000
        },
        "instructions": [
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 12
          }
         },
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 3
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 7,
           "name": "divide",
           "stackTransition": {
            "__class__": "Association",
            "key": 2,
            "value": 1
           }
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 20,
           "name": "turnOn",
           "stackTransition": {
            "__class__": "Association",
            "key": 1,
            "value": 0
           }
          }
         }
        ],
        "locals": [],
        "name": "default",
        "ticking": true,
        "nextRun": 540224,
        "lastStart": 493
       }
      ],
      "variables": [
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 1000
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 12
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 3
       }
      ]
     });
     for(let i = 0; i < 3; i++){
      sim.execute();
    }
    assert.equal(4, sim.stack[0]);
  });

  it('addition', function(){ // 3 + 4
    sim.loadProgram({
      "__class__": "UziProgram",
      "scripts": [
       {
        "__class__": "UziScript",
        "arguments": [],
        "delay": {
         "__class__": "UziVariable",
         "name": null,
         "value": 1000
        },
        "instructions": [
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 3
          }
         },
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 4
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 6,
           "name": "add",
           "stackTransition": {
            "__class__": "Association",
            "key": 2,
            "value": 1
           }
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 20,
           "name": "turnOn",
           "stackTransition": {
            "__class__": "Association",
            "key": 1,
            "value": 0
           }
          }
         }
        ],
        "locals": [],
        "name": "default",
        "ticking": true,
        "nextRun": 152608,
        "lastStart": 18
       }
      ],
      "variables": [
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 1000
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 3
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 4
       }
      ]
     });
     for(let i = 0; i < 3; i++){
      sim.execute();
    }
    assert.equal(7, sim.stack[0]);
  });

  it('subtraction', function(){ // 8 - 4
    sim.loadProgram({
      "__class__": "UziProgram",
      "scripts": [
       {
        "__class__": "UziScript",
        "arguments": [],
        "delay": {
         "__class__": "UziVariable",
         "name": null,
         "value": 1000
        },
        "instructions": [
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 8
          }
         },
         {
          "__class__": "UziPushInstruction",
          "argument": {
           "__class__": "UziVariable",
           "name": null,
           "value": 4
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 8,
           "name": "subtract",
           "stackTransition": {
            "__class__": "Association",
            "key": 2,
            "value": 1
           }
          }
         },
         {
          "__class__": "UziPrimitiveCallInstruction",
          "argument": {
           "__class__": "UziPrimitive",
           "code": 20,
           "name": "turnOn",
           "stackTransition": {
            "__class__": "Association",
            "key": 1,
            "value": 0
           }
          }
         }
        ],
        "locals": [],
        "name": "default",
        "ticking": true
       }
      ],
      "variables": [
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 1000
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 8
       },
       {
        "__class__": "UziVariable",
        "name": null,
        "value": 4
       }
      ]
     });
    for(let i = 0; i < 3; i++){
      sim.execute();
    }
    assert.equal(4, sim.stack[0]);
  })
});
