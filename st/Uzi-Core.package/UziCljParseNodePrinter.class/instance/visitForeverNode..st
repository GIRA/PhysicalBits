visiting
visitForeverNode: node
	self writeLine: '(ast/forever-node '.
	self indent: 1
		during: [self indent;
			visit: node body;
			write: ')']