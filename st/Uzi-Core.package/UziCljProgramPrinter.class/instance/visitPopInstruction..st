encoding instructions
visitPopInstruction: instruction
	| var |
	var := instruction argument.
	self write: '(emit/write-global "{1}")' format: { var name }