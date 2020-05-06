printing
print: anUziParseNode on: aStream
	indent := 0.
	stream := aStream.
	intervals := Dictionary new.
	self visit: anUziParseNode.
	^ stream contents