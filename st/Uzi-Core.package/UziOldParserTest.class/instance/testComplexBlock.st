tests
testComplexBlock
	| pp b kvp |
	pp := parser block.
	kvp := pp parse: '[
		foo.
		bar baz.
		1 foo: [bar * baz]
	] '.
	b := kvp value.
	self assert: 3 equals: b statements size