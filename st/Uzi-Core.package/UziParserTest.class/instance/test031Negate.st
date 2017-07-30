tests
test031Negate
	| ast pp |
	pp := parser expression end.
	ast := pp parse: '!read(D13)'.
	self assert: #not equals: ast selector.
	self assert: #read equals: ast arguments first selector.
	self assert: 13 equals: ast arguments first arguments first value.