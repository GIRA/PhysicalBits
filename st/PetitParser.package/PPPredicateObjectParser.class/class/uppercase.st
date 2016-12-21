factory-chars
uppercase
	^ self on: (PPCharSetPredicate on: [ :char | char isUppercase ]) message: 'uppercase letter expected'