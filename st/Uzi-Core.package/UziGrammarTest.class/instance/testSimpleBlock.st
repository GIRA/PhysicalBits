grammar-block
testSimpleBlock
	| pp b |
	pp := UziGrammar new block.
	b := pp parse: '[foo]'.
	self assert: b first inputValue = '['.
	self assert: b second size = 1.
	self assert: b third inputValue = ']'