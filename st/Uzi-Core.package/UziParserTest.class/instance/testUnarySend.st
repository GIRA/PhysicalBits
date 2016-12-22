tests
testUnarySend
	| pp expr |
	pp := parser unarySend.
	expr := pp parse: '1 foo bar'.
	self assert: 1 equals: expr receiver receiver value.
	self assert: #foo equals: expr receiver selector.
	self assert: 0 equals: expr receiver arguments size.
	self assert: #bar equals: expr selector.
	self assert: 0 equals: expr arguments size.