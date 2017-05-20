printing
print: anUziProgram on: aStream
	indent := 0.
	stream := aStream.
	self visit: anUziProgram.
	^ stream contents