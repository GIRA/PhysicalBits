grammar-scripts
testTickingRateMinutes
	| pp |
	pp := UziParser new tickingRate.
	self assert: (pp parse: '1/m') delay = 1 minute.
	self assert: (pp parse: '2/m') delay = 30 seconds.
	self should: [pp parse: '0/m'] raise: InvalidTickingRateError.
	self should: [pp parse: '-1/m'] raise: InvalidTickingRateError.