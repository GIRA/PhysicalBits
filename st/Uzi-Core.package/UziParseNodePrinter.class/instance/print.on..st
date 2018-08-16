printing
print: anUziParseNode on: aStream
	indent := 0.
	stream := aStream.
	self visit: anUziParseNode.
	^ stream contents