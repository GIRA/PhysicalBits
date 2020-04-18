encoding instructions
visitJNZInstruction: instruction
	self write: '(emit/jnz {1})' format: { instruction argument }