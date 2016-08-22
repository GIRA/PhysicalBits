visiting
visitMessageSend: node
	| instruction |
	"First, we visit the receiver (if it's not nil)"
	node receiver ifNotNil: [:rcvr | self visit: rcvr].
	"Then, we visit the arguments"
	self visitCollection: node arguments.
	"Then we create the primitive call instruction.
	The argument is a primitive name associated with the node selector."
	instruction := UziPrimitiveCallInstruction 
		arg: (self primitiveFor: node selector).
	"Finally, we register the instruction"
	instructions add: instruction