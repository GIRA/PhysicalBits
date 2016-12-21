responses
sendResponseCode: code content: aString type: contentType close: aBool
	"Send a 500 Internal server error response"

	^self sendResponseCode: code content: aString type: contentType do:[:hdr|
		aBool ifTrue:[hdr headerAt: 'Connection' put: 'close'].
	].