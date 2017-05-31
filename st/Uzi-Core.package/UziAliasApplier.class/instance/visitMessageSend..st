visiting
visitMessageSend: node
	^ (node class
		receiver: (self visit: node receiver)
		selector: node selector
		arguments: (self visitCollection: node arguments)
		primitive: node primitive)
		token: node token