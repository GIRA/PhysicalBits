encoding instructions
visitJMPInstruction: instruction
	self write: '(emit/jmp {1})' format: { instruction argument }