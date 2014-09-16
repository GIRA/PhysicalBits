grammar-scripts
testTickingState
	| pp |
	pp := UziGrammar new tickingState.
	self assert: (pp parse: 'ticking') inputValue = 'ticking'.
	self assert: (pp parse: 'normal') inputValue = 'normal'.
	self assert: (pp parse: 'paused') inputValue = 'paused'.
	self assert: (pp parse: 'foo') isPetitFailure.