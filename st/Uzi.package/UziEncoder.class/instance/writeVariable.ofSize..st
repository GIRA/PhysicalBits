encoding programs
writeVariable: var ofSize: size
	"If the size equals 4 we have to encode it as a float"
	| value |
	value := size = 4 
		ifTrue: [var value asFloat asIEEE32BitWord]
		ifFalse: [var value].
	(size - 1 to: 0 by: -1) do: [:n |
		stream nextPut: ((value bitShift: (8 * n * -1)) bitAnd: 16rFF)]