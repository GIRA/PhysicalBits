tests
test035While
	| ast body pp |
	pp := parser while end.
	ast := pp parse: 'while 1 { turnOn(D13); }'.
	self assert: 1 equals: ast condition value.
	self assert: 1 equals: ast body statements size.
	body := ast body.
	self assert: #turnOn equals: body statements first selector.
	self assert: 13 equals: body statements first arguments first value.