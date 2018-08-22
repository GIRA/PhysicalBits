tests
test017TickingRateMinutes
	| pp |
	pp := parser tickingRate.
	self assert: (pp parse: '1/m') delay = 1 minute.
	self assert: (pp parse: '2/m') delay = 30 seconds.
	self assert: (pp parse: '0/m') delay > (365 days * 1000).
	self assert: (pp parse: '-1/m') delay < 0 days.