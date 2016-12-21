factory-objects
anyExceptAnyOf: aCollection
	^ self
		on: [ :each | (aCollection includes: each) not ] message: 'any except ' , aCollection printString , ' expected'
		negated: [ :each | aCollection includes: each ] message: aCollection printString ,  ' not expected'