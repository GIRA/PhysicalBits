tests
testReturn
	| pp expr |
	pp := parser return.
	expr := pp parse: '^ 5'.
	self assert: 5 equals: expr value value