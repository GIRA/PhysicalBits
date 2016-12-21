factory-chars
hex
	^ self 
		on: (PPCharSetPredicate on: [ :char | 
			(char between: $0 and: $9) 
				or: [ (char between: $a and: $f) 
				or: [ (char between: $A and: $F) ] ] ])
		message: 'hex digit expected'