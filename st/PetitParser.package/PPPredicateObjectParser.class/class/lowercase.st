factory-chars
lowercase
	^ self on: (PPCharSetPredicate on: [ :char | char isLowercase ]) message: 'lowercase letter expected'