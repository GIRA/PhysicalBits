tests
test031Negate
	| ast pp |
	pp := parser expression end.
	ast := pp parse: '!read(D13)'.
	self assert: #! equals: ast selector.
	self assert: #read equals: ast arguments first value selector.
	self assert: 13 equals: ast arguments first value arguments first value value.