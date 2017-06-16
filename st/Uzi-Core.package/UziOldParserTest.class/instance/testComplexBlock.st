tests
testComplexBlock
	| pp b |
	pp := parser block.
	b := pp parse: '[
		foo.
		bar baz.
		1 foo: [bar * baz]
	] '.
	self assert: 3 equals: b statements size