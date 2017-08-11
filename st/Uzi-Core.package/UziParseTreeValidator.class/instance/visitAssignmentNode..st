visiting
visitAssignmentNode: node
	self assertVariable: node left.
	self assertExpression: node right.
		
	self visit: node left;
		visit: node right