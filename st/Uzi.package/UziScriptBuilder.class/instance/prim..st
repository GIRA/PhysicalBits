instruction creation
prim: primName
	| arg |
	arg := UziPrimitive named: primName.
	instructions add: (UziPrimitiveCallInstruction arg: arg)