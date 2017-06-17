tests
test015TickingRateDays
	| pp |
	pp := parser tickingRate.
	self assert: (pp parse: '1/d') delay = 1 day.
	self assert: (pp parse: '2/d') delay = 12 hours.
	self should: [pp parse: '0/d'] raise: UziInvalidTickingRateError.
	self should: [pp parse: '-1/d'] raise: UziInvalidTickingRateError.