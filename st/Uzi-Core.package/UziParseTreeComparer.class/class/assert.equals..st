as yet unclassified
assert: ast1 equals: ast2
	^ (self basicNew initializeWith: { ast1 . ast2 }) compare