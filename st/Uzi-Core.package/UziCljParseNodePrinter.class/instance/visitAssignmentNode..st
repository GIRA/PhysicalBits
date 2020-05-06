visiting
visitAssignmentNode: node
	self writeLine: '(ast/assignment-node'.
	self indent: 1
		during: [self 
			indent; visit: node left; writeLine: '';
			indent; 	visit: node right;
			write: ')']