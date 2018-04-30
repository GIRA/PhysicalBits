encoding instructions
visitStopScriptInstruction: instruction
	self indent;
		saveInterval: [self write: 'stop: {1}' format: { instruction argument asSymbol storeString }]