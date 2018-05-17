visiting
visitVariableNode: node
	self assert: (self variablesInScope anySatisfy: [:def | def name = node name])
		description: 'Undefined variable found'
		node: node
	