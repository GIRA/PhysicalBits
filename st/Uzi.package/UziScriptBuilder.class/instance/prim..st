bytecode creation
prim: primName
	| arg |
	arg := UziPrimitive named: primName.
	bytecodes add: (UziPrimitiveCallBytecode arg: arg)