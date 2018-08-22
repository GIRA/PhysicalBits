visiting
visitForeverNode: node
	self saveIntervalFor: node
		during: [self write: 'forever '; visit: node body]