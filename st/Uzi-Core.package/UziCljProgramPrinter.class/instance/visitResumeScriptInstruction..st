encoding instructions
visitResumeScriptInstruction: instruction
	self write: '(emit/resume "{1}")' format: { instruction argument }