visiting
visitReturnNode: node
	node value
		ifNil: [self write: '(ast/return-node)']
		ifNotNil: [:val |
			self write: '(ast/return-node ';
				visit: val;
				write: ')']