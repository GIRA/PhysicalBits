assertions
assertScriptReference: node
	^ self assert: node isScriptReference
		description: 'Script reference expected'
		node: node