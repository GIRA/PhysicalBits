bytecode creation
jmp: arg
	^ self add: (UziJMPBytecode arg: arg)