visiting
visitProgramNode: node
	| scriptNames |
	program := node.
	
	node globals do: [:each | 
		self assertVariableDeclaration: each.
		each value ifNotNil: [:v | self assertNumber: v]].
	
	scriptNames := Set new.
	node scripts do: [:each | 
		self assertScript: each.
		(self assert: (scriptNames includes: each name) not
			description: 'Script name already in use'
			node: each)
			ifTrue: [scriptNames add: each name]].
		
	self visitCollection: node globals;
		visitCollection: node scripts 
	