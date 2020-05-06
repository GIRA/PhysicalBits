visiting
writeDoUntil: node
	self writeLine: '(ast/do-until-node'.
	self indent: 1
		during: [self
				indent; visit: node condition; writeLine: '';
				indent; visit: node post;
				write: ')']