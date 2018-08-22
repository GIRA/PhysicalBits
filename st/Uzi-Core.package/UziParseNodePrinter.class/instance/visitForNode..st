visiting
visitForNode: node
	self saveIntervalFor: node
		during: [
			self write: 'for {1} = ' format: { node counter name };
				visit: node start;
				write: ' to ';
				visit: node stop.
			node step ifNotNil: [:step | self write: ' by '; visit: step].
			self write: ' '; visit: node body]