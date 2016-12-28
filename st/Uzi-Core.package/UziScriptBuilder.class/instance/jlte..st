instruction creation
jlte: arg
	self addInstruction: (UziJLTEInstruction arg: arg)