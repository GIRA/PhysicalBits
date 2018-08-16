visiting
visitAssignmentNode: node
	self visit: node left;
		write: ' = ';
		visit: node right