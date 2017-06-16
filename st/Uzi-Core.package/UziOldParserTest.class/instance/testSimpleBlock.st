tests
testSimpleBlock
	| pp b |
	pp := parser block.
	b := pp parse: '[foo]'.
	self assert: 1 equals: b statements size