visiting
visitLogicalAndNode: node
	self assertExpression: node left.
	self assertExpression: node right.
		
	self visit: node left;
		visit: node right