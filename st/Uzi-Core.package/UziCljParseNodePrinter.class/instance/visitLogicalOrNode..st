visiting
visitLogicalOrNode: node
	self halt.
	self saveIntervalFor: node
		during: [
			self write: '(';
				visit: node left;
				write: ' || ';
				visit: node right;
				write: ')']