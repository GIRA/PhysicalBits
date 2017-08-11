assertions
assertVariableDeclaration: node
	^ self assert: node isVariableDeclaration
		description: 'Variable declaration expected' 
		node: node