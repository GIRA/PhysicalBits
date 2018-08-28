parsing
parse: aString
	| ast |
	ast := super parse: aString.
	ast isPetitFailure ifFalse: [self resolvePrimitiveCalls: ast].
	^ ast