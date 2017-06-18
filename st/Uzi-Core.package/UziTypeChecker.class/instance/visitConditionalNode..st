visiting
visitConditionalNode: node
	self assertExpression: node condition.
	self assertBlock: node trueBranch nlocals: 0.
	self assertBlock: node falseBranch nlocals: 0.
	
	self visit: node condition;
		visit: node trueBranch;
		visit: node falseBranch 