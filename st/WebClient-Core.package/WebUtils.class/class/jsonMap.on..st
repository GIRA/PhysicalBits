json-encode
jsonMap: aDictionary on: stream
	"Encodes a dictionary"

	stream nextPut: ${.
	"Sorting keys ensures deterministic order"
	aDictionary keys asArray sort do:[:key|
		self jsonString: key on: stream.
		stream nextPutAll:': '.
		self jsonObj: (aDictionary at: key) on: stream.
	] separatedBy:[stream nextPutAll: ', '].
	stream nextPut: $}.