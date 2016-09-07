grammar-block
testComplexBlock
	| pp b |
	pp := UziGrammar new block.
	b := pp parse: ' 	
	[
		foo.
		bar baz.
		1 foo: bar * baz
	] '.
	self assert: b first inputValue = '['.
	self assert: b second size = 3.
	self assert: b third inputValue = ']'