accessing
size
	"Return the number of bytes necessary to encode this variable.
	If the value is negative or float then the size is 4 bytes. Also, the
	max number of bytes is 4."
	^ (value isFloat or: [value < 0])
		ifTrue: [4]
		ifFalse: [(1 to: 4) 
			detect: [:size | value < (2 raisedTo: 8 * size)]
			ifNone: 4]