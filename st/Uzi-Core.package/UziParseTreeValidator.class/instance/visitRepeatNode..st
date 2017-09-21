visiting
visitRepeatNode: node
	self assertExpression: node times;
		assertBlock: node body.
		
	self visit: node times;
		visit: node body