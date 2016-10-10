encoding
encode: anUziProgram
	stream := #[] writeStream.
	self visit: anUziProgram.
	^ stream contents