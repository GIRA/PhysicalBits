tests
test046DoUntil
	| ast body pp |
	pp := parser doUntil end.
	ast := pp parse: 'do { turnOn(D13); } until 1 ;'.
	self assert: 1 equals: ast condition value.
	self assert: ast body equals: ast pre.
	self assert: 1 equals: ast body statements size.
	self assert: ast negated.
	body := ast body.
	self assert: #turnOn equals: body statements first selector.
	self assert: 13 equals: body statements first arguments first value.