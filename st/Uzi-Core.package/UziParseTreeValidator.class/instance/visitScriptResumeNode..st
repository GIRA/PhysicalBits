visiting
visitScriptResumeNode: node
	| validScriptNames |
	validScriptNames := self program scripts collect: [:each | each name].
	node scripts do: [:scriptName | 
		self assert: (validScriptNames includes: scriptName)
			description: 'Invalid script'
			node: node.
			
		(self program scriptNamed: scriptName)
			ifNotNil: [:script |
				self assert: script isTask
					description: 'Task reference expected'
					node: node]]