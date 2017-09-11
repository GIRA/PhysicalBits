visiting
visitScriptStartNode: node
	node scripts do: [:ref | (self assertScriptReference: ref)
		ifTrue: [(program scriptNamed: ref scriptName)
			ifNotNil: [:script |
				self assert: script isTask
					description: 'Task reference expected'
					node: node]]]