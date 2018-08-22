visiting
visitYieldNode: node
	self saveIntervalFor: node during: [self write: 'yield']