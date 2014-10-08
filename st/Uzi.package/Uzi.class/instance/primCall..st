bytecode creation
primCall: primNameOrNumber
	| arg |
	arg := primNameOrNumber isNumber
		ifTrue: [primNameOrNumber]
		ifFalse: [self class primitives at: primNameOrNumber].
	bytecodes add: (UziPrimitiveCallBytecode arg: arg)