grammar-block
testEmptyBlock
	| pp |
	pp := UziGrammar new block.
	self shouldnt: [pp parse: '[]'] raise: Exception.