assertions
assertVariableDefinition: node
	^ self assert: node isVariableDefinition 
		description: 'Variable definition expected' 
		node: node