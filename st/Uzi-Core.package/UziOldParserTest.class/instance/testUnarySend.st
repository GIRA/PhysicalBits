tests
testUnarySend
	| pp expr |
	pp := parser unarySend.
	expr := pp parse: '1 foo bar'.
	self assert: #foo equals: expr arguments first selector.
	self assert: 1 equals: expr arguments first arguments first value.
	self assert: 1 equals: expr arguments first arguments size.
	self assert: #bar equals: expr selector.
	self assert: 1 equals: expr arguments size.