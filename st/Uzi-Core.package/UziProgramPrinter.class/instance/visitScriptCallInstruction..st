encoding instructions
visitScriptCallInstruction: instruction
	self indent;
		saveInterval: [self write: 'call: {1}' format: { instruction argument asSymbol storeString }]