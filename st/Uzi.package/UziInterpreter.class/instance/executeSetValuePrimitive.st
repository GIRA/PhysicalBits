primitives
executeSetValuePrimitive
	| pop1 pop2 |
	pop1 := stack pop.
	pop2 := stack pop.
	self setPin: pop2 value: pop1