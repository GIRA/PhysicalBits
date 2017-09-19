visiting
visitReturnNode: node
	node value 
		ifNotNil: [self assertExpression: node value;
			visit: node value]