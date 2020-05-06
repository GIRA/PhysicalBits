visiting
visitPinLiteralNode: node
	self saveIntervalFor: node
		during: [self write: '(ast/literal-pin-node "{1}" {2})' format: { node type . node number }]