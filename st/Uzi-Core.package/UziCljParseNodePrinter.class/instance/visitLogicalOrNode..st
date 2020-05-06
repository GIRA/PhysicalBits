visiting
visitLogicalOrNode: node
	self writeLine: '(ast/logical-or-node'.
	self indent: 1
		during: [self 
			indent; visit: node left; writeLine: '';
			indent; visit: node right;
			write: ')']