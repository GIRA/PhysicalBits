grammar-scripts
testEmptyScriptBody
	| pp b |
	pp := UziGrammar new scriptBody.
	b := pp parse: '[]'.
	self assert: (b first inputValue = '[' and: [b third inputValue = ']']).
	self assert: (pp parse: 'cualquier cosa []') isPetitFailure