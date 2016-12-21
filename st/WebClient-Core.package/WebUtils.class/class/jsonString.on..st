json-encode
jsonString: aString on: stream
	"Encodes a string"

	stream nextPut: $".
	aString do:[:ch| self jsonChar: ch on: stream].
	stream nextPut: $".
