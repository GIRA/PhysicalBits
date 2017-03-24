visiting
visitSpecialVariableNode: node
	self assert: node primitive notNil
		description: ('Special variable #{1} not found' format: { node name })
		node: node.