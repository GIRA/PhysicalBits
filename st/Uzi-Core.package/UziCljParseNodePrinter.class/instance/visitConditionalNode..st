visiting
visitConditionalNode: node
	self writeLine: '(ast/conditional-node'.
	self indent: 1
		during: [
			self indent; visit: node condition;
				writeLine: '';
				indent; visit: node trueBranch;
				writeLine: '';
				indent; visit: node falseBranch;
				write: ')']