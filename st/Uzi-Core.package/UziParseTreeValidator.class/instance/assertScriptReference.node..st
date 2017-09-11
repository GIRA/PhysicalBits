assertions
assertScriptReference: scriptName node: node
	| validScriptNames |
	validScriptNames := program scripts collect: [:each | each name].
	self assert: (validScriptNames includes: scriptName)
		description: 'Invalid script'
		node: node