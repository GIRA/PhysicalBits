visiting
visitReturnNode: node
	node value 
		ifNil: [(script prim: #ret) node: node]
		ifNotNil: [self visit: node value.
			(script prim: #retv) node: node]