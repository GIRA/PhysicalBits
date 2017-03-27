visiting
visitScriptRefNode: node
	| validScriptNames |
	validScriptNames := program scripts collect: [:each | each name].
	self assert: (validScriptNames includes: node name)
		description: 'Invalid script'
		node: node