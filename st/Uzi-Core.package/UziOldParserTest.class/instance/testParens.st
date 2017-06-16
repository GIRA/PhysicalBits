tests
testParens
	| a pp b c |
	pp := parser subexpression.
	a := pp parse: '(foo)'.
	self assert: 'foo' equals: a name.
	b := pp parse: '( 1 foo   )  '.
	self assert: 1 equals: b arguments first value.
	self assert: b selector = #foo.
	self assert: b arguments size = 1.
	c := pp parse: '(5)'.	
	self assert: c value = 5