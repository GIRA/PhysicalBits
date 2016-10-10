receiving
processPinValue
	"
	The PIN_VALUE msg is composed of 3 bytes:
		- the pin number
		- the value (MSB)
		- the value (LSB)
		
	However, when saving the value it must be in the range 0..1
	"
	| pin value val1 val2 count |
	count := serial next.
	count timesRepeat: [
		val1 := serial next.
		val2 := serial next.
		pin := val1 >> 2.
		value := val2 bitOr: ((val1 bitAnd: 2r11) << 8).
		pinValues at: pin put: value / 1023.0]