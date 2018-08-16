visiting
visitForNode: node
	self write: 'for {1} = ' format: { node counter name };
		visit: node start;
		write: ' to ';
		visit: node stop.
	node step 
		ifNotNil: [:step |
			self write: ' by ';
				visit: step].
	self write: ' ';
		visit: node body
		