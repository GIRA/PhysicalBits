grammar
testParens
	| a pp b c |
	pp := UziParser new parens.
	a := pp parse: '(foo)'.
	self assert: 'foo' equals: a name.
	b := pp parse: '  ( 1 foo   )  '.
	self assert: 1 equals: b receiver value.
	self assert: b selector = #foo.
	self assert: b arguments isEmpty.
	c := pp parse: '(5)'.	
	self assert: c value = 5