visiting
visitScriptStartNode: node
	self assertScriptReference: node.
	
	(program scriptNamed: node scriptName)
		ifNotNil: [:script |
			self assert: script isTask
				description: 'Task reference expected'
				node: node]