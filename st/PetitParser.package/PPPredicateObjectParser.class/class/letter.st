factory-chars
letter
	^ self on: (PPCharSetPredicate on: [ :char | char isLetter ]) message: 'letter expected'