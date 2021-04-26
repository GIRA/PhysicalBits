

function run() {
  console.log("Running...");

  var suite = new Benchmark.Suite;
  // add tests
  /*
  var temp = null;
  suite.add('JSON.parse(..)', function() {
    temp = JSON.parse(test_data);
  });
  suite.add('JSONX.parse(..)', function() {
    temp = JSONX.parse(test_data);
  });
  */
  /*
  test_data = JSONX.parse(test_data);
  var temp = null;
  suite.add('JSON.stringify(..)', function() {
    temp = JSON.stringify(test_data);
  });
  suite.add('JSONX.stringify(..)', function() {
    temp = JSONX.stringify(test_data);
  });
  */

  test_data = JSONX.parse(test_data);
  var temp = null;
  suite.add('deepClone (JSONX)', function () {
    temp = JSONX.parse(JSONX.stringify(test_data));
  });
  suite.add('deepClone (utils.js)', function () {
    temp = deepClone(test_data)
  });

  // add listeners
  suite.on('cycle', function(event) {
    console.log(String(event.target));
  });
  suite.on('complete', function() {
    console.log('Fastest is ' + this.filter('fastest').map('name'));
    console.log(temp);
  });

  suite.run();
}

var test_data = '{"connection":{"isConnected":true,"portName":"COM9","availablePorts":["COM6","COM7","COM9"]},"memory":{"arduino":148,"uzi":960},"tasks":[{"scriptName":"loop","isRunning":true,"isError":false}],"output":[],"pins":{"timestamp":7898090,"available":[{"name":"D2","reporting":false},{"name":"D3","reporting":false},{"name":"D4","reporting":false},{"name":"D5","reporting":false},{"name":"D6","reporting":false},{"name":"D7","reporting":false},{"name":"D8","reporting":true},{"name":"D9","reporting":false},{"name":"D10","reporting":false},{"name":"D11","reporting":false},{"name":"D12","reporting":false},{"name":"D13","reporting":true},{"name":"A0","reporting":false},{"name":"A1","reporting":false},{"name":"A2","reporting":false},{"name":"A3","reporting":false},{"name":"A4","reporting":false},{"name":"A5","reporting":false}],"elements":[{"name":"D8","number":8,"value":0.0},{"name":"D13","number":13,"value":0.0}]},"globals":{"timestamp":7898100,"available":[{"name":"action","reporting":true},{"name":"s","reporting":true},{"name":"nan","reporting":true},{"name":"pinf","reporting":true},{"name":"ninf","reporting":true}],"elements":[{"name":"action","number":3,"value":0.0,"raw-bytes":[0,0,0,0]},{"name":"s","number":4,"value":0.2670982,"raw-bytes":[62,136,193,24]},{"name":"nan","number":5,"value":{"___NAN___":0},"raw-bytes":[127,192,0,0]},{"name":"pinf","number":10,"value":{"___INF___":-1},"raw-bytes":[255,128,0,0]},{"name":"ninf","number":13,"value":{"___INF___":1},"raw-bytes":[127,128,0,0]}]},"pseudo-vars":{"timestamp":7898140,"available":[{"name":"__delta","reporting":true},{"name":"__delta_smooth","reporting":true},{"name":"__report_interval","reporting":true}],"elements":[{"name":"__delta","value":-4,"ts":7898140},{"name":"__delta_smooth","value":1.8,"ts":7898140},{"name":"__report_interval","value":0,"ts":7898140}]},"program":{"type":"uzi","src":"var action = 0;\\nvar s = 0;\\n\\nvar ninf = Infinity;\\nvar pinf = -Infinity;\\nvar nan = 1;\\n\\ntask loop() running 1000/s {\\n\\taction = isOn(D8);\\n\\twrite(D13, action);\\n\\ts = (sin((millis() / 500)) * 1);\\n\\t\\n    ninf = Infinity;\\n    pinf = -Infinity;\\n    nan = NaN;\\n}","compiled":{"__class__":"UziProgram","globals":[{"__class__":"UziVariable","name":"action","value":0},{"__class__":"UziVariable","name":"s","value":0},{"__class__":"UziVariable","value":1},{"__class__":"UziVariable","name":"nan","value":1},{"__class__":"UziVariable","value":8},{"__class__":"UziVariable","value":13},{"__class__":"UziVariable","value":500},{"__class__":"UziVariable","value":{"___INF___":-1}},{"__class__":"UziVariable","name":"pinf","value":{"___INF___":-1}},{"__class__":"UziVariable","value":{"___NAN___":0}},{"__class__":"UziVariable","value":{"___INF___":1}},{"__class__":"UziVariable","name":"ninf","value":{"___INF___":1}}],"scripts":[{"__class__":"UziScript","arguments":[],"delay":{"__class__":"UziVariable","value":1},"instructions":[{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","value":8}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","name":"isOn"}},{"__class__":"UziPopInstruction","argument":{"__class__":"UziVariable","name":"action","value":0}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","value":13}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","name":"action","value":0}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","name":"write"}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","name":"millis"}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","value":500}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","name":"divide"}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","name":"sin"}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","value":1}},{"__class__":"UziPrimitiveCallInstruction","argument":{"__class__":"UziPrimitive","name":"multiply"}},{"__class__":"UziPopInstruction","argument":{"__class__":"UziVariable","name":"s","value":0}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","value":{"___INF___":1}}},{"__class__":"UziPopInstruction","argument":{"__class__":"UziVariable","name":"ninf","value":0}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","value":{"___INF___":-1}}},{"__class__":"UziPopInstruction","argument":{"__class__":"UziVariable","name":"pinf","value":0}},{"__class__":"UziPushInstruction","argument":{"__class__":"UziVariable","value":{"___NAN___":0}}},{"__class__":"UziPopInstruction","argument":{"__class__":"UziVariable","name":"nan","value":0}}],"locals":[],"name":"loop","running?":true,"once?":false}]},"ast":{"__class__":"UziProgramNode","imports":[],"globals":[{"__class__":"UziVariableDeclarationNode","name":"action","value":{"__class__":"UziNumberLiteralNode","value":0}},{"__class__":"UziVariableDeclarationNode","name":"s","value":{"__class__":"UziNumberLiteralNode","value":0}},{"__class__":"UziVariableDeclarationNode","name":"ninf","value":{"__class__":"UziNumberLiteralNode","value":{"___INF___":1}}},{"__class__":"UziVariableDeclarationNode","name":"pinf","value":{"__class__":"UziNumberLiteralNode","value":{"___INF___":-1}}},{"__class__":"UziVariableDeclarationNode","name":"nan","value":{"__class__":"UziNumberLiteralNode","value":1}}],"scripts":[{"__class__":"UziTaskNode","name":"loop","arguments":[],"body":{"__class__":"UziBlockNode","statements":[{"__class__":"UziAssignmentNode","left":{"__class__":"UziVariableNode","name":"action"},"right":{"__class__":"UziCallNode","selector":"isOn","arguments":[{"__class__":"Association","key":null,"value":{"__class__":"UziPinLiteralNode","type":"D","number":8}}]}},{"__class__":"UziCallNode","selector":"write","arguments":[{"__class__":"Association","key":null,"value":{"__class__":"UziPinLiteralNode","type":"D","number":13}},{"__class__":"Association","key":null,"value":{"__class__":"UziVariableNode","name":"action"}}]},{"__class__":"UziAssignmentNode","left":{"__class__":"UziVariableNode","name":"s"},"right":{"__class__":"UziCallNode","selector":"*","arguments":[{"__class__":"Association","key":null,"value":{"__class__":"UziCallNode","selector":"sin","arguments":[{"__class__":"Association","key":null,"value":{"__class__":"UziCallNode","selector":"/","arguments":[{"__class__":"Association","key":null,"value":{"__class__":"UziCallNode","selector":"millis","arguments":[]}},{"__class__":"Association","key":null,"value":{"__class__":"UziNumberLiteralNode","value":500}}]}}]}},{"__class__":"Association","key":null,"value":{"__class__":"UziNumberLiteralNode","value":1}}]}},{"__class__":"UziAssignmentNode","left":{"__class__":"UziVariableNode","name":"ninf"},"right":{"__class__":"UziNumberLiteralNode","value":{"___INF___":1}}},{"__class__":"UziAssignmentNode","left":{"__class__":"UziVariableNode","name":"pinf"},"right":{"__class__":"UziNumberLiteralNode","value":{"___INF___":-1}}},{"__class__":"UziAssignmentNode","left":{"__class__":"UziVariableNode","name":"nan"},"right":{"__class__":"UziNumberLiteralNode","value":{"___NAN___":0}}}]},"state":"running","tickingRate":{"__class__":"UziTickingRateNode","value":1000,"scale":"s"}}],"primitives":[]}}}';
