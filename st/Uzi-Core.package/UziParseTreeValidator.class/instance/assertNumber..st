assertions
assertNumber: node
	^ self assert: node isNumberLiteral
		description: 'Number expected'
		node: node