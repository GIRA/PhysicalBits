visiting
visitForNode: node
	self assertVariableDeclaration: node counter;
		assertExpression: node start;
		assertExpression: node stop;
		assertExpression: node step;
		assertBlock: node body.
		
	self visit: node counter;
		visit: node start;
		visit: node stop;
		visit: node step;
		visit: node body