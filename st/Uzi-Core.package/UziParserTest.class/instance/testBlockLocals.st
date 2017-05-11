tests
testBlockLocals
	| pp block |
	pp := parser block.
	block := pp parse: '[:a || b, c = 2 |]'.
	self assert: 1 equals: block arguments size.
	self assert: 2 equals: block locals size.
	self assert: #a equals: block arguments first name.
	self assert: #b equals: block locals first name.
	self assert: #c equals: block locals second name