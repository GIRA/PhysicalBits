tests
test016TickingRateHours
	| pp |
	pp := parser tickingRate.
	self assert: (pp parse: '1/h') delay = 1 hour.
	self assert: (pp parse: '2/h') delay = 30 minutes.
	self should: [pp parse: '0/h'] raise: UziInvalidTickingRateError.
	self should: [pp parse: '-1/h'] raise: UziInvalidTickingRateError.