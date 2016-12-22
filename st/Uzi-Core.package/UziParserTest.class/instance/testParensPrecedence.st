tests
testParensPrecedence
	| pp expr arg |
	pp := parser expression.
	expr := pp parse: '3 + (4 * 5)'.
	self assert: expr receiver value = 3.
	self assert: expr selector = #+.
	self assert: expr arguments size = 1.
	arg := expr arguments first.
	self assert: arg receiver value = 4.
	self assert: arg selector = #*.
	self assert: arg arguments size = 1.
	self assert: arg arguments first value = 5.