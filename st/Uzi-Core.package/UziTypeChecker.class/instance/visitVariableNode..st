visiting
visitVariableNode: node
	self assert: ((program variableDefinitions, script arguments, script locals)
			anySatisfy: [:def | def name = node name])
		description: 'Undefined variable found'
		node: node
	