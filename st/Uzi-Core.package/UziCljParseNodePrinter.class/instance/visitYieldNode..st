visiting
visitYieldNode: node
	self halt.
	self saveIntervalFor: node during: [self write: 'yield']