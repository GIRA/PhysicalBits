responses
stream200Response: aStream size: streamSize type: contentType
	"Stream a 200 OK response"

	^self stream200Response: aStream size: streamSize type: contentType do:[:resp].