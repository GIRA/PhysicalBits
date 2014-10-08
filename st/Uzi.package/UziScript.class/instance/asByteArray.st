converting
asByteArray
	| stream |
	stream := #[] writeStream.
	self writeHeaderOn: stream;
		writeLiteralsOn: stream;
		writeVariablesOn: stream;
		writeBytecodesOn: stream.
	^ stream contents