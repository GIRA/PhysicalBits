tests
test036Parens
	| pp |
	pp := parser expression end.
	self assert: 1 equals: (pp parse: '(1)') value.
	self assert: 10 equals: (pp parse: '   (  10   ) ') value.
	self assert: 0.5 equals: (pp parse: '(0.5)') value.