visiting
visitConditionalNode: node
	self assertExpression: node condition.
	self assertBlock: node trueBranch nargs: 0 nlocals: 0.
	self assertBlock: node falseBranch nargs: 0 nlocals: 0.
	
	self visit: node condition;
		visit: node trueBranch;
		visit: node falseBranch 