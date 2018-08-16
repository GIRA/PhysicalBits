visiting
visitTickingRateNode: node
	self write: '{1}/{2}' format: { node value . node scale }