factory-chars
digit
	^ self on: (PPCharSetPredicate on: [ :char | char isDigit ]) message: 'digit expected'