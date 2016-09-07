visiting-utils
visitCollection: aCollection
	^ aCollection collect: [:each | self visit: each]