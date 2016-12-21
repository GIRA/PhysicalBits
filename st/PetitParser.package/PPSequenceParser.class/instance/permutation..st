operators-mapping
permutation: anArrayOfIntegers
	"Answer a permutation of the receivers sequence."
	
	anArrayOfIntegers do: [ :index |
		(index isInteger and: [ index between: 1 and: parsers size ])
			ifFalse: [ self error: 'Invalid permutation index: ' , index printString ] ].
	^ self ==> [ :nodes | anArrayOfIntegers collect: [ :index | nodes at: index ] ]