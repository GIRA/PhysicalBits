visiting
visitTickingRateNode: node
	self saveIntervalFor: node 
		during: [self write: '(ast/ticking-rate-node {1} "{2}")' format: { node value . node scale }]