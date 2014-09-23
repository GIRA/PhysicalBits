grammar
testParens
	| a pp b c |
	pp := UziParser new parens.
	a := pp parse: '(foo)'.
	self assert: a receiver isNil.
	self assert: a selector = #foo.
	self assert: a arguments isEmpty.
	b := pp parse: '  ( foo   )  '.
	self assert: b receiver isNil.
	self assert: b selector = #foo.
	self assert: b arguments isEmpty.
	c := pp parse: '(5)'.	
	self assert: c value = 5