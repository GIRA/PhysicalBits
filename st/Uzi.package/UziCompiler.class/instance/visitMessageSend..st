visiting
visitMessageSend: node
	(self handleSpecialMessageSend: node)
		ifFalse: [node receiver ifNotNil: [:rcvr | self visit: rcvr].
			self visitCollection: node arguments.
			script prim: (self primitiveFor: node selector)]