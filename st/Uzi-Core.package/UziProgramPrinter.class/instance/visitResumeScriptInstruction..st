encoding instructions
visitResumeScriptInstruction: instruction
	self indent;
		saveInterval: [self write: 'resume: {1}' format: { instruction argument asSymbol storeString }]