visiting
visitPrimitiveCall: node
	"Regular message send generates a primitive call"
	self visitCollection: node arguments.
	(script prim: node primitive name) node: node