tests
testKeywordSend
	| pp expr |
	pp := parser keywordSend.
	expr := pp parse: '1 foo: 2 + 3 bar baz: 4'.
	self assert: 1 equals: expr receiver value.
	self assert: #'foo:baz:' equals: expr selector.
	self assert: 2 equals: expr arguments size.
	self assert: 2 equals: expr arguments first receiver value.
	self assert: #+ equals: expr arguments first selector.
	self assert: 1 equals: expr arguments first arguments size.
	self assert: 3 equals: expr arguments first arguments first receiver value.
	self assert: #bar equals: expr arguments first arguments first selector.
	self assert: 0 equals: expr arguments first arguments first arguments size.
	self assert: 4 equals: expr arguments second value