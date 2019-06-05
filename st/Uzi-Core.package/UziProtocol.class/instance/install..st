installing
install: anUziProgram
	| encoded lsb msb result |
	encoded := Uzi encode: anUziProgram.
	msb := (encoded size bitAnd: 16rFF00) >> 8.
	lsb := (encoded size bitAnd: 16rFF).
	result := self send: {
		self class msgOutSaveProgram.
		msb.
		lsb		
	}, encoded.
	UziOutput log: 'Installed program successfully!'; flush.
	^ result