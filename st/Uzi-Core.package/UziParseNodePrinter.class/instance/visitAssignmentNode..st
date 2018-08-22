visiting
visitAssignmentNode: node
	self saveIntervalFor: node 
		during: [self 
			visit: node left;
			write: ' = ';
			visit: node right]