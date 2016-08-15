encoding
encode: anUziProgram
	^ self 
		withStream: #[] writeStream 
		do: [self visit: anUziProgram]