assertions
assertLiteral: node
	^ self assert: (node isNumberLiteral or: [node isPinLiteral])
		description: 'Literal expected'
		node: node