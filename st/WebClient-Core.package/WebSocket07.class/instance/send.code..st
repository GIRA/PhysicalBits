read/write
send: data code: opcode
	"Sends unfragmented data using the given opcode"

	^self send: data code: opcode final: true