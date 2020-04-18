encoding instructions
visitStopScriptInstruction: instruction
	self write: '(emit/stop "{1}")' format: { instruction argument }