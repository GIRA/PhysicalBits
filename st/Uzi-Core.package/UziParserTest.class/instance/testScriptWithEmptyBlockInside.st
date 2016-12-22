tests
testScriptWithEmptyBlockInside
	| pp b |
	pp := parser script.
	b := pp parse: '#main ticking 1/s [[]]'.
	self assert: b body size = 1.
	self assert: b body first statements isEmpty 