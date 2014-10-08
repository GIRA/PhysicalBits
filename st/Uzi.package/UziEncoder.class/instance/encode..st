encoding
encode: anUziScript
	stream := #[] writeStream.
	self visit: anUziScript.
	^ stream contents