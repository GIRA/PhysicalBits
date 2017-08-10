assertions
assertNumber: node
	^ self assert: node isNumber
		description: 'Number expected'
		node: node