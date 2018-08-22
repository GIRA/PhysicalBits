tests
test015TickingRateDays
	| pp |
	pp := parser tickingRate.
	self assert: (pp parse: '1/d') delay = 1 day.
	self assert: (pp parse: '2/d') delay = 12 hours.
	self assert: (pp parse: '0/d') delay > (365 days * 1000).
	self assert: (pp parse: '-1/d') delay < 0 days.