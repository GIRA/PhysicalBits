responses
send200Response: aString contentType: contentType do: aBlock
	"Send a 200 OK response"

	| resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: 200.
	resp headerAt: 'Content-Type' put: contentType.
	aBlock value: resp.
	^self sendResponse: resp content: aString.