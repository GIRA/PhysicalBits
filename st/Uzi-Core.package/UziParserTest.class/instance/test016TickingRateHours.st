tests
test016TickingRateHours
	| pp |
	pp := parser tickingRate.
	self assert: (pp parse: '1/h') delay = 1 hour.
	self assert: (pp parse: '2/h') delay = 30 minutes.
	self assert: (pp parse: '0/h') delay > (365 days * 1000).
	self assert: (pp parse: '-1/h') delay < 0 days.