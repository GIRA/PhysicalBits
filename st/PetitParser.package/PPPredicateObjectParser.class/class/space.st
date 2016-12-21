factory-chars
space
	^ self on: (PPCharSetPredicate on: [ :char | char isSeparator ]) message: 'separator expected'