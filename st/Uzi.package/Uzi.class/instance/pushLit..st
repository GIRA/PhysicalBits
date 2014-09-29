bytecode creation
pushLit: arg
	^ self add: (UziPushLiteralBytecode arg: arg)