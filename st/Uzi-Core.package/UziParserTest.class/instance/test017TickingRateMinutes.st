tests
test017TickingRateMinutes
	| pp |
	pp := parser tickingRate.
	self assert: (pp parse: '1/m') delay = 1 minute.
	self assert: (pp parse: '2/m') delay = 30 seconds.
	self should: [pp parse: '0/m'] raise: UziInvalidTickingRateError.
	self should: [pp parse: '-1/m'] raise: UziInvalidTickingRateError.