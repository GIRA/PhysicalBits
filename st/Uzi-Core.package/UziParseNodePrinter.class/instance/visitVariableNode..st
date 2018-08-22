visiting
visitVariableNode: node
	self saveIntervalFor: node during: [self write: node name]