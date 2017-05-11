visiting
visitLoopNode: node
	self assertBlock: node pre nargs: 0 nlocals: 0.
	self assertExpression: node condition.
	self assertBlock: node post nargs: 0 nlocals: 0.
	
	self visit: node pre;
		visit: node condition;
		visit: node post