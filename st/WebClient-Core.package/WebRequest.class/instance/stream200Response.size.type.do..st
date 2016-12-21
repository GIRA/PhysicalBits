responses
stream200Response: aStream size: streamSize type: contentType do: aBlock
	"Stream a 200 OK response"

	| resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: 200.
	resp headerAt: 'Content-Type' put: contentType.
	aBlock value: resp.
	^self sendResponse: resp contentStream: aStream size: streamSize.