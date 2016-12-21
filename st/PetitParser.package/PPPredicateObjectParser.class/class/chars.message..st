factory-chars
chars: aCollection message: aString
	^ self on: (PPCharSetPredicate on: [ :char | aCollection includes: char ]) message: aString