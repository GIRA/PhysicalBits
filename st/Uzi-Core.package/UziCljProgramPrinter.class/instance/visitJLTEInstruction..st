encoding instructions
visitJLTEInstruction: instruction
	self write: '(emit/jlte {1})' format: { instruction argument }