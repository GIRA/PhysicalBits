visiting
visitConditionalNode: node
	self assertExpression: node condition.
	self assertBlock: node trueBranch.
	self assertBlock: node falseBranch.
	
	self visit: node condition;
		visit: node trueBranch;
		visit: node falseBranch 