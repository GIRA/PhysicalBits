accessing
scriptForPC: anInteger
	^ protocol currentProgram
		ifNotNil: [:p | p scriptForPC: anInteger]