grammar-blocks
testEmptyBlock
	| pp b |
	pp := UziParser new block.
	b := pp parse: '[]'.
	self assert: b statements isEmpty