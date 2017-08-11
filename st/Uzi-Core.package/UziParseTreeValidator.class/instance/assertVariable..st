assertions
assertVariable: node
	^ self assert: node isVariable
		description: 'Variable expected'
		node: node