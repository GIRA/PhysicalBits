visiting
handlePrimitiveCall: node
	node arguments do: [:arg | self visit: arg value].
	(script prim: node primitive name) node: node