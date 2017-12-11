encoding instructions
visitPauseScriptInstruction: instruction
	self indent;
		saveInterval: [self write: 'pause: #{1}' format: { instruction argument }]