responses
stream200Response: aStream size: streamSize
	"Stream a 200 OK response"

	^self stream200Response: aStream size: streamSize type: 'application/octet-stream'