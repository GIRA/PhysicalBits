accessing
sortedGlobals
	^ (variables 
		asSortedCollection: [:a :b | 
			a size = b size
			ifTrue: [a value = b value
				ifTrue: [(a name isNil and: [b name notNil])
					ifTrue: [true]
					ifFalse: [(b name isNil and: [a name notNil])
						ifTrue: [false]
						ifFalse: [(a name notNil and: [b name notNil])
							ifTrue: [a name < b name]
							ifFalse: [true]]]]
				ifFalse: [a value < b value]]
			ifFalse: [a size < b size]])
		asArray