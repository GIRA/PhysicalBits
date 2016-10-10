visiting
visitAssignmentNode: node
	self visit: node right.
	script pop: node left name