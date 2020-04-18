encoding instructions
visitPauseScriptInstruction: instruction
	self write: '(emit/pause "{1}")' format: { instruction argument }