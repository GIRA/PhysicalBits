json-encode
jsonEncode: anObject
	"Encode the given object as JSON"

	^String streamContents:[:s| self jsonObj: anObject on: s]