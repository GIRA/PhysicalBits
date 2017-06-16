tests
testKeywordSend
	| pp expr expr1 expr2 |
	pp := parser keywordSend.
	expr := pp parse: '1 foo: 2 + 3 bar baz: 4'.
	self assert: #'foo:baz:' equals: expr selector.
	self assert: 3 equals: expr arguments size.
	self assert: 1 equals: expr arguments first value.
	expr1 := expr arguments second.
	self assert: #+ equals: expr1 selector.
	self assert: 2 equals: expr1 arguments size.
	self assert: 2 equals: expr1 arguments first value.
	expr2 := expr1 arguments second.
	self assert: #bar equals: expr2 selector.
	self assert: 1 equals: expr2 arguments size.	
	self assert: 3 equals: expr2 arguments first value.
	self assert: 4 equals: expr arguments third value