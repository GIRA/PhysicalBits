visiting
visitPinLiteralNode: node
	self assert: (#(D A) includes: node type)
		description: 'Invalid pin type'
		node: node.
	self assert: node number isInteger
		description: 'Integer expected'
		node: node