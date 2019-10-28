visiting
visitProgramNode: node
	| imports scriptNames globals |	
	imports := Set new.
	node imports do: [:each |
		self assertImport: each;
			assert: (imports includes: each alias -> each path) not
			description: 'Library already imported'
			node: each.
		imports add: each alias -> each path].
	
	globals := Set new.
	node globals do: [:each | 
		self assertVariableDeclaration: each.
		each value ifNotNil: [:v | self assertLiteral: v].
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
		
	self visitCollection: node imports;
		visitCollection: node globals;
		visitCollection: node scripts 
	