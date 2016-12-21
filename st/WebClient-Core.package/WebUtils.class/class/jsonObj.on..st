json-encode
jsonObj: anObject on: stream
	"Encode a generic object"

	anObject isString ifTrue:[^self jsonString: anObject on: stream].
	anObject isNumber ifTrue:[^self jsonNumber: anObject on: stream].
	anObject == nil ifTrue:[^stream nextPutAll: 'null'].
	anObject == true ifTrue:[^stream nextPutAll: 'true'].
	anObject == false ifTrue:[^stream nextPutAll: 'false'].
	anObject isArray ifTrue:[^self jsonArray: anObject on: stream].
	anObject isDictionary ifTrue:[^self jsonMap: anObject on: stream].

	self error: 'Cannot encode: ', anObject