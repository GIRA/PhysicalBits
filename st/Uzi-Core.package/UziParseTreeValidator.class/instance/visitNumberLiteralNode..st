visiting
visitNumberLiteralNode: node
	self assert: node value isNumber
		description: 'Number expected'
		node: node