instance creation
on: aBlock message: aString size: anInteger
	^ self on: aBlock message: aString negated: [ :each | (aBlock value: each) not ] message: 'no ' , aString size: anInteger 