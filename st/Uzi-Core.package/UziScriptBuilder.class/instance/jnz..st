instruction creation
jnz: arg
	^ self addInstruction: (UziJNZInstruction arg: arg)