visiting
visitLoopNode: node
	self assertBlock: node pre.
	self assertExpression: node condition.
	self assertBlock: node post.
	
	self visit: node pre;
		visit: node condition;
		visit: node post