tests
test018TickingRateSeconds
	| pp |
	pp := parser tickingRate.
	self assert: (pp parse: '1/s') delay = 1 second.
	self assert: (pp parse: '2/s') delay = 500 milliSeconds.
	self assert: (pp parse: '0/s') delay > (365 days * 1000).
	self assert: (pp parse: '-1/s') delay < 0 days.