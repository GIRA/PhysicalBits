visiting
visitAssignmentNode: node
	self assert: node left isVariable
		description: 'Variable expected'
		node: node left.
	self assert: node right isExpression
		description: 'Expression expected'
		node: node right.
		
	self visit: node left;
		visit: node right