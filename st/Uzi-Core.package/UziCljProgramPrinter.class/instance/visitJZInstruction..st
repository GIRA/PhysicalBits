encoding instructions
visitJZInstruction: instruction
	self write: '(emit/jz {1})' format: { instruction argument }