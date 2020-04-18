encoding instructions
visitReadLocalInstruction: instruction
	| var |
	var := instruction argument.
	self write: '(emit/read-local "{1}")' format: { var name }