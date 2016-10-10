primitives
executeGetValuePrimitive
	| pop |
	pop := stack pop.
	stack push: (protocol getPinValue: pop)