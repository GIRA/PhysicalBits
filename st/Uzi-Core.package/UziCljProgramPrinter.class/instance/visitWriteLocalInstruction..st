encoding instructions
visitWriteLocalInstruction: instruction
	| var |
	var := instruction argument.
	self write: '(emit/write-local "{1}")' format: { var name }