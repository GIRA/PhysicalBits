assertions
assertExpression: node
	self assert: node isExpression
		description: 'Expression expected'
		node: node