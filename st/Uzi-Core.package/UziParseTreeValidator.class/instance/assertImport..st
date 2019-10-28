assertions
assertImport: node
	^ self assert: node isImport
		description: 'Import expected' 
		node: node