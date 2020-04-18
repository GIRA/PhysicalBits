encoding instructions
visitStartScriptInstruction: instruction
	self write: '(emit/start "{1}")' format: { instruction argument }