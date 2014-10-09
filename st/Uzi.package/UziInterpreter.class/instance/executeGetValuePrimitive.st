primitives
executeGetValuePrimitive
	| pop |
	pop := stack pop.
	stack push: (self getPinValue: pop)