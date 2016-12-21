read/write
send: data code: opcode final: isFinal
	"Sends unfragmented data using the given opcode"

	^self send: data code: opcode final: isFinal mask: self masking