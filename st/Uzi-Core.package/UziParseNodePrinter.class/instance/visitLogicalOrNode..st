visiting
visitLogicalOrNode: node
	self write: '(';
		visit: node left;
		write: ' || ';
		visit: node right;
		write: ')'