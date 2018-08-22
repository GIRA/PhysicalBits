visiting
visitLogicalOrNode: node
	self saveIntervalFor: node
		during: [
			self write: '(';
				visit: node left;
				write: ' || ';
				visit: node right;
				write: ')']