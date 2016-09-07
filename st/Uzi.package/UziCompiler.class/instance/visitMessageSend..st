visiting
visitMessageSend: node
	(self handleSpecialMessageSend: node)
		ifFalse: ["Regular message send generates a primitive call"
			node receiver ifNotNil: [:rcvr | self visit: rcvr].
			self visitCollection: node arguments.
			script prim: (self primitiveFor: node selector)]