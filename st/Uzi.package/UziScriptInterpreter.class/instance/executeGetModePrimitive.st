primitives
executeGetModePrimitive
	| pop |
	pop := stack pop.
	stack push: (protocol getPinMode: pop)