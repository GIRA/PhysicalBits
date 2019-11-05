visiting
visitNumberLiteralNode: node
	self saveIntervalFor: node
		during: [self write: node value asString]