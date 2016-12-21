control requests
handleControl: payload type: opcode
	"Handle a control opcode"

	opcode caseOf: {
		[8]	-> [self handleClose: payload].
		[9]	-> [self handlePing: payload].
		[10] -> [self handlePong: payload].
	} otherwise:[self error: 'Unknown control request: ', opcode].

