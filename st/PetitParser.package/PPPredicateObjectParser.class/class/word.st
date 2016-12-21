factory-chars
word
	^ self on: (PPCharSetPredicate on: [ :char | char isAlphaNumeric ]) message: 'letter or digit expected'