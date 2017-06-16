tests
testTickingState
	| pp |
	pp := parser tickingState.
	self assert: (pp parse: 'ticking') value == #ticking.