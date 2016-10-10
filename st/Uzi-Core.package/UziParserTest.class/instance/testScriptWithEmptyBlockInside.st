grammar-blocks
testScriptWithEmptyBlockInside
	| pp b |
	pp := UziParser new script.
	b := pp parse: '#main ticking 1/s [[]]'.
	self assert: b body size = 1.
	self assert: b body first statements isEmpty 