assertions
assertScriptReference: node
	| validScriptNames |
	(self assert: node isScriptReference
		description: 'Script reference expected' 
		node: node)
		ifFalse: [^ false].
	
	validScriptNames := program scripts collect: [:each | each name].
	^ self assert: (validScriptNames includes: node scriptName)
		description: 'Invalid script'
		node: node