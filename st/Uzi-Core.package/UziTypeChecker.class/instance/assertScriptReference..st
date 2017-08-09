assertions
assertScriptReference: node
	| validScriptNames |
	validScriptNames := program scripts collect: [:each | each name].
	self assert: (validScriptNames includes: node scriptName)
		description: 'Invalid script'
		node: node