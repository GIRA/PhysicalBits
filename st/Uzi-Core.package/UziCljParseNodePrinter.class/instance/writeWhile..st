visiting
writeWhile: node
	self writeLine: '(ast/while-node'.
	self indent: 1
		during: [self
				indent; visit: node condition; writeLine: '';
				indent; visit: node post;
				write: ')']