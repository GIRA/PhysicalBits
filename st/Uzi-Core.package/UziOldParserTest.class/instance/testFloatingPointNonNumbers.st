tests
testFloatingPointNonNumbers
	| pp |
	pp := parser number.
	self assert: (pp parse: 'Infinity') value = Float infinity.
	self assert: (pp parse: '-Infinity') value = (Float infinity * -1).
	self assert: (pp parse: 'NaN') value isNaN