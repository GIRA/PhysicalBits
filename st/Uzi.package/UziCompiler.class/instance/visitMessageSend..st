visiting
visitMessageSend: node
	node receiver ifNotNil: [:rcvr | self visit: rcvr].
	self visitCollection: node arguments.
	script prim: (self primitiveFor: node selector)