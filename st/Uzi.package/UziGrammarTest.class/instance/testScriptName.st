as yet unclassified
testScriptName
	| pp |
	pp := UziGrammar new scriptName.
	self assert: (pp parse: '#main') second inputValue = 'main'.
	self assert: (pp parse: '#foo') first = $#.
	self assert: (pp parse: 'richo') isPetitFailure