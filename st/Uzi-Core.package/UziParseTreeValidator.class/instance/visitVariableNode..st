visiting
visitVariableNode: node
	self assert: ((program variableDeclarations, script arguments, script locals)
			anySatisfy: [:def | def name = node name])
		description: 'Undefined variable found'
		node: node
	