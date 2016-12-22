tests
testKeywordSelector
	| pp |
	pp := parser keywordSelector.
	self assert: 'foo:' equals: (pp parse: 'foo:').
	self assert: (pp parse: 'foo') isPetitFailure