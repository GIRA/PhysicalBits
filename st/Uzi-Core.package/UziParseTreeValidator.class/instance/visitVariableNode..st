visiting
visitVariableNode: node
	self assert: ((program globals, script arguments, script locals)
			anySatisfy: [:def | def name = node name])
		description: 'Undefined variable found'
		node: node
	