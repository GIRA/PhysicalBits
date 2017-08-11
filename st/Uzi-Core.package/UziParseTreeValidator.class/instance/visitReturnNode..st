visiting
visitReturnNode: node
	self assertExpression: node value;
		visit: node value