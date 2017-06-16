tests
testBinarySend
	| pp msg msg2 |
	pp := parser binarySend.
	msg := pp parse: '1 + 2 foo'.
	self assert: #+ equals: msg selector.
	self assert: 2 equals: msg arguments size.
	self assert: 1 equals: msg arguments first value.
	msg2 := msg arguments second.
	self assert: #foo equals: msg2 selector.
	self assert: 1 equals: msg2 arguments size.
	self assert: 2 equals: msg2 arguments first value.