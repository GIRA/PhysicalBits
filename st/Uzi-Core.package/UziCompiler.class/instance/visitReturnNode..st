visiting
visitReturnNode: node
	self visit: node value.
	(script prim: #retv) node: node