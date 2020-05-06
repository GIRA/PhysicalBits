visiting
visitRepeatNode: node
	self writeLine: '(ast/repeat-node '.
	self indent: 1
		during: [self indent;
			visit: node times;
			writeLine: '';
			indent;
			visit: node body;
			write: ')']