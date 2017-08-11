assertions
assertStatement: node
	^ self assert: node isStatement
		description: 'Statement expected' 
		node: node