primitives and tokens
testKeywordToken
	| pp |
	pp := UziGrammar new keywordToken.
	self assert: (pp parse: 'foo:') inputValue = 'foo:'.
	self assert: (pp parse: 'foo') isPetitFailure