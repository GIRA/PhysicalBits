visiting
visitVariableNode: node
	self assert: ((self program globals, self script arguments, self script locals)
			anySatisfy: [:def | def name = node name])
		description: 'Undefined variable found'
		node: node
	