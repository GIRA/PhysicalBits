encoding instructions
visitPrimitiveCallInstruction: instruction
	self indent;
		write: 'prim: #{1}' format: { instruction argument name }