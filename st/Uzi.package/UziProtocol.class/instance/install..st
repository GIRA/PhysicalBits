installing
install: anUziProgram
	| encoded lsb msb |
	encoded := UziEncoder new encode: anUziProgram.
	msb := (encoded size bitAnd: 16rFF00) >> 8.
	lsb := (encoded size bitAnd: 16rFF).
	self send: {
		6.
		msb.
		lsb		
	}, encoded