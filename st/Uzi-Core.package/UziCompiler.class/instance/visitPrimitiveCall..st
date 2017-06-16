visiting
visitPrimitiveCall: node
	self visitCollection: node arguments.
	(script prim: node primitive name) node: node