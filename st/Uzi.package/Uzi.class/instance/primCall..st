bytecode creation
primCall: arg
	^ self add: (UziPrimitiveCallBytecode arg: arg)