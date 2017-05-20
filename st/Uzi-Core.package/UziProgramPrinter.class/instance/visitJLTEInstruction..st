encoding instructions
visitJLTEInstruction: instruction
	self indent;
		write: 'jlte: {1}' format: { instruction argument }