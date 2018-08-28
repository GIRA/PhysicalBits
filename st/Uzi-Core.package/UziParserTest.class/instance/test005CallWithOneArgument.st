tests
test005CallWithOneArgument
	| pp ast |
	pp := parser call end.
	ast := pp parse: 'toggle(D13)'.
	self assert: #toggle equals: ast selector.
	self assert: 1 equals: ast arguments size.
	self assert: 13 equals: ast arguments first value value