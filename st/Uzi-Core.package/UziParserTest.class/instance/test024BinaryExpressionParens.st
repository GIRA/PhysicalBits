tests
test024BinaryExpressionParens
	| pp expr arg |
	pp := parser expression end.
	expr := pp parse: '3 + (4 * 5)'.
	self assert: expr selector = #+.
	self assert: expr arguments size = 2.
	self assert: expr arguments first value value = 3.
	arg := expr arguments second value.
	self assert: arg selector = #*.
	self assert: arg arguments size = 2.
	self assert: arg arguments first value value = 4.
	self assert: arg arguments second value value = 5.