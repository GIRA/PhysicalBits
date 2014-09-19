as yet unclassified
testLiteralNumber
	| pp |
	pp := UziParser new numberLiteral.
	self assert: (pp parse: '5') value = 5.
	self assert: (pp parse: '15') value = 15.
	self assert: (pp parse: '-5') value = -5.
	self assert: (pp parse: '-15') value = -15.
	self assert: ((pp parse: '5.2') value closeTo: 5.2).
	self assert: ((pp parse: '-5.2') value closeTo: -5.2).