visiting
visitMessageSend: node
	| bytecode |
	"First, we visit the receiver (if it's not nil)"
	node receiver ifNotNil: [:rcvr | self visit: rcvr].
	"Then, we visit the arguments"
	self visitCollection: node arguments.
	"Then we create the primCall bytecode.
	The argument is a primitive name associated with the node selector."
	bytecode := UziPrimitiveCallBytecode 
		arg: (self primitiveFor: node selector).
	"Finally, we register the bytecode"
	bytecodes add: bytecode