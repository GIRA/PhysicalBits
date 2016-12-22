tests
testBinarySend
	| pp msg |
	pp := parser binarySend.
	msg := pp parse: '1 + 2 foo'.
	self assert: 1 equals: msg receiver value.
	self assert: #+ equals: msg selector.
	self assert: 1 equals: msg arguments size.
	self assert: 2 equals: msg arguments first receiver value.
	self assert: #foo equals: msg arguments first selector.
	self assert: 0 equals: msg arguments first arguments size