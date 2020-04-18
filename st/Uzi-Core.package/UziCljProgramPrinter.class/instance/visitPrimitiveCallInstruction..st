encoding instructions
visitPrimitiveCallInstruction: instruction
	self write: '(emit/prim-call "{1}")' format: { instruction argument name }