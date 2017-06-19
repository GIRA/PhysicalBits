grammar-binary
binarySelector
	^ (PPPredicateObjectParser
		on: [:each | each isAlphaNumeric not
				and: [each isSeparator not
						and: [(self forbiddenCharacters includes: each) not]]]
		message: 'Not binary') plus flatten 