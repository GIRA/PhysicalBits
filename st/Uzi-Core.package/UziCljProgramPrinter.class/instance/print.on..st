printing
print: anUziProgram on: aStream
	intervals := OrderedCollection new.
	indent := 0.
	stream := aStream.
	self visit: anUziProgram.
	^ stream contents