encoding instructions
visitStartScriptInstruction: instruction
	self indent;
		saveInterval: [self write: 'start: {1}' format: { instruction argument asSymbol storeString }]