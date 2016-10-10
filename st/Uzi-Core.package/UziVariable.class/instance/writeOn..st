encoding
writeOn: stream
	"If the size equals 4 we have to encode it as a float"
	| size actualValue |
	size := self size.
	actualValue := size = 4 
		ifTrue: [value asFloat asIEEE32BitWord]
		ifFalse: [value].
	(size - 1 to: 0 by: -1) do: [:n |
		stream nextPut: ((actualValue bitShift: (8 * n * -1)) bitAnd: 16rFF)]