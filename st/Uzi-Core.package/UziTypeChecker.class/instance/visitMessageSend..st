visiting
visitMessageSend: node
	| numArgs |
	numArgs := node selector numArgs.
	self assert: numArgs = node arguments size
		description: ('{1} argument{2} expected' format: { numArgs . numArgs = 1 ifTrue: '' ifFalse: 's' })
		node: node.
	self assert: node primitive notNil
		description: ('Invalid primitive #{1}' format: { node selector })
		node: node.
	
	self visit: node receiver;
		visitCollection: node arguments