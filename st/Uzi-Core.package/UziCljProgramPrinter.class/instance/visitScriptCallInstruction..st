encoding instructions
visitScriptCallInstruction: instruction
	self write: '(emit/script-call "{1}")' format: { instruction argument }