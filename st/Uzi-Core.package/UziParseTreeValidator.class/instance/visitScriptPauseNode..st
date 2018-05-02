visiting
visitScriptPauseNode: node
	node scripts do: [:ref | (self assertScriptReference: ref)
		ifTrue: [(self program scriptNamed: ref scriptName)
			ifNotNil: [:script |
				self assert: script isTask
					description: 'Task reference expected'
					node: node]]]