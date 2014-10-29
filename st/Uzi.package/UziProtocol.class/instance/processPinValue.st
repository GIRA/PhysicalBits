receiving
processPinValue
	"
	The PIN_VALUE msg is composed of 3 bytes:
		- the pin number
		- the value (MSB)
		- the value (LSB)
		
	However, when saving the value it must be in the range 0..1
	"
	| pin value val1 val2 |
	pin := self getArgument: serial next or: [^ self].
	val1 := self getArgument: serial next or: [^ self].
	val2 := self getArgument: serial next or: [^ self].
	value := val2 bitOr: (val1 << 7).
	pinValues at: pin put: value / 1023.0