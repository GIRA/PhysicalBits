as yet unclassified
testTickingState
	| pp |
	pp := UziParser new tickingState.
	self assert: (pp parse: 'ticking') value == #ticking.