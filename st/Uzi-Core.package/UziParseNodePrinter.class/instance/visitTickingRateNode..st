visiting
visitTickingRateNode: node
	self saveIntervalFor: node 
		during: [self write: '{1}/{2}' format: { node value . node scale }]