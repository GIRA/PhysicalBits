instruction creation
prim: primName
	| arg |
	arg := UziPrimitive named: primName.
	^ self addInstruction: (UziPrimitiveCallInstruction arg: arg)