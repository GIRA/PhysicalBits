grammar-scripts
testScriptName
	| pp |
	pp := UziGrammar new scriptName.
	self assert: (pp parse: '#main') inputValue = '#main'.
	self assert: (pp parse: '#foo') inputValue first = $#.
	self assert: (pp parse: 'richo') isPetitFailure.
	self assert: (pp parse: '#     foo') isPetitFailure