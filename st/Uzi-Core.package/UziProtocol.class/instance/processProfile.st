receiving
processProfile
	"
	The PROFILE msg is composed of 2 bytes:
		- the tick count (MSB)
		- the tick count (LSB)
	"
	| value val1 val2 |
	Transcript show: 'PROFILE '.
	val1 := serial next.
	val2 := serial next.
	value := val2 bitOr: (val1 << 7).
	Transcript show: ('Ticks per 100 ms: {1}' format: { value }); cr
