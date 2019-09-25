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
		
		stmt isAssignment 
			ifTrue: [self assertLiteral: stmt right.
				self assert: (node isResolved not 
						or: [self program globals anySatisfy: [:g | g name = (node alias, '.', stmt left name)]])
					description: 'Undefined variable found'
					node: stmt left].
		stmt isScriptControl ifTrue: [
			stmt scriptNames do: [:scriptName |
				self assert: (node isResolved not
						or: [(self program scriptNamed: node alias, '.', scriptName) notNil])
					description: 'Invalid script'
					node: stmt]	]]