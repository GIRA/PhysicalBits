tests
test039DoWhile
	| ast body pp |
	pp := parser doWhile end.
	ast := pp parse: 'do { turnOn(D13); } while 1 ;'.
	self assert: 1 equals: ast condition value.
	self assert: ast body equals: ast pre.
	self assert: 1 equals: ast body statements size.
	body := ast body.
	self assert: #turnOn equals: body statements first selector.
	self assert: 13 equals: body statements first arguments first value value.