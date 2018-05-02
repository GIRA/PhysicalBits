visiting
visitProgramNode: node
	| scriptNames globals |	
	globals := Set new.
	node globals do: [:each | 
		self assertVariableDeclaration: each.
		each value ifNotNil: [:v | self assertNumber: v].
		self assert: (globals includes: each name) not
			description: 'Global variable already declared'
			node: each.
		globals add: each name].
	
	scriptNames := Set new.
	node scripts do: [:each | 
		self assertScript: each;
			assert: (scriptNames includes: each name) not
			description: 'Script name already in use'
			node: each.
		scriptNames add: each name].
		
	self visitCollection: node globals;
		visitCollection: node scripts 
	