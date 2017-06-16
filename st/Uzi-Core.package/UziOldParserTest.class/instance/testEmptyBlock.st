tests
testEmptyBlock
	| pp |
	pp := parser block.
	self shouldnt: [pp parse: '[]'] raise: Exception.