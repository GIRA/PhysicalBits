visiting
visitLoopNode: node
	self assertBlock: node pre nlocals: 0.
	self assertExpression: node condition.
	self assertBlock: node post nlocals: 0.
	
	self visit: node pre;
		visit: node condition;
		visit: node post