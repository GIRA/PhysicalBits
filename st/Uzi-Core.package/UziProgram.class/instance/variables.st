accessing
variables
	^ (variables 
		asSortedCollection: [:a :b | a size = b size
			ifTrue: [a value < b value]
			ifFalse: [a size < b size]])
		asArray
		
