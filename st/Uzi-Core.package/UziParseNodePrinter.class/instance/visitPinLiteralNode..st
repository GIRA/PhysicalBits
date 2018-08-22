visiting
visitPinLiteralNode: node
	self saveIntervalFor: node
		during: [self write: '{1}{2}' format: { node type . node number }]