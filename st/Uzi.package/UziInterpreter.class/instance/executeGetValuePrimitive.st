primitives
executeGetValuePrimitive
	| pop |
	pop := stack pop.
	stack push: (self getValue: pop)