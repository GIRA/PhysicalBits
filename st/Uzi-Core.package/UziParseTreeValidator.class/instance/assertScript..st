assertions
assertScript: node
	^ self assert: node isScript
		description: 'Script expected' 
		node: node