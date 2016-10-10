primitives and tokens
testIdentifierToken
	| pp |
	pp := UziGrammar new identifierToken.
	self assert: (pp parse: 'Richo') inputValue = 'Richo'.
	self assert: (pp parse: 'r') inputValue = 'r'.
	self assert: (pp parse: '23richo') isPetitFailure