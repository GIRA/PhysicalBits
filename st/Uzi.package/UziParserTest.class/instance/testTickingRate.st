as yet unclassified
testTickingRate
	| pp |
	pp := UziParser new tickingRate.
	self assert: (pp parse: '1/s') delay = 1 second.
	self assert: (pp parse: '2/s') delay = 500 milliSeconds.
	self should: [pp parse: '0/s'] raise: InvalidTickingRateError.
	self should: [pp parse: '-1/s'] raise: InvalidTickingRateError.