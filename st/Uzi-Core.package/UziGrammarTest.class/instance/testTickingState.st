grammar-scripts
testTickingState
	| pp |
	pp := UziGrammar new tickingState.
	self assert: (pp parse: 'ticking') inputValue = 'ticking'.
	self assert: (pp parse: 'foo') isPetitFailure.