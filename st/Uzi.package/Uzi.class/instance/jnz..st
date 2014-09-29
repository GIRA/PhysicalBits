bytecode creation
jnz: arg
	^ self add: (UziJNZBytecode arg: arg)