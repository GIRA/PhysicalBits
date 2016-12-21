json-encode
jsonArray: anArray on: stream
	"Encodes an array"

	stream nextPut: $[.
	anArray 
		do:[:each| self jsonObj: each on: stream]
		separatedBy:[stream nextPutAll:', '].
	stream nextPut:$].
