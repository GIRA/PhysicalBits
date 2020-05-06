visiting
visitVariableNode: node
	self write: '(ast/variable-node "{1}")' format: { node name }