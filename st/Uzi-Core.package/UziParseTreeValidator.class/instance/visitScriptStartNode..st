visiting
visitScriptStartNode: node
	node scriptNames do: [:scriptName |
		self assertScriptReference: scriptName node: node].
	
	node scriptNames do: [:scriptName |
		(program scriptNamed: scriptName)
			ifNotNil: [:script |
				self assert: script isTask
					description: 'Task reference expected'
					node: node]]