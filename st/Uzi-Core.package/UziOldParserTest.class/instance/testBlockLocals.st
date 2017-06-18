tests
testBlockLocals
	| pp block args kvp |
	pp := parser block.
	kvp := pp parse: '[:a || b, c = 2 |]'.
	args := kvp key.
	block := kvp value.
	self assert: 1 equals: args size.
	self assert: 2 equals: block locals size.
	self assert: #a equals: args first name.
	self assert: #b equals: block locals first name.
	self assert: #c equals: block locals second name