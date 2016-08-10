bytecode creation
primCall: primName
	| arg |
	arg := UziPrimitive named: primName.
	bytecodes add: (UziPrimitiveCallBytecode arg: arg)