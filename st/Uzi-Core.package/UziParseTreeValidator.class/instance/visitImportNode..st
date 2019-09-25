visiting
visitImportNode: node
	| initBlock |
	initBlock := node initializationBlock.
	initBlock ifNil: [^ self].
	
	self assertBlock: initBlock.
	initBlock statements do: [:stmt |
		self assertStatement: stmt.
		self assert: (stmt isAssignment or: [stmt isScriptControl])
			description: 'Assignment or script control statement expected'
			node: stmt.
			
		stmt isAssignment ifTrue: [self assertLiteral: stmt right]]