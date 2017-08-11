visiting
visitPrimitiveCall: node
	node primitive ifNotNil: [:prim || numArgs |
		numArgs := prim stackTransition key.
		self assert: numArgs = node arguments size
			description: ('{1} argument{2} expected' 
				format: { numArgs . numArgs = 1 ifTrue: '' ifFalse: 's' })
			node: node].
	self assert: node primitive notNil
		description: ('Invalid primitive ''{1}''' format: { node selector })
		node: node.	
	node arguments do: [:each | self assertExpression: each].
	
	self visitCollection: node arguments