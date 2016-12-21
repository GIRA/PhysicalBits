responses
sendResponseCode: code content: aString type: contentType do: aBlock
	"Send a 500 Internal server error response"

	| resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: code.
	contentType ifNotNil:[resp headerAt: 'Content-Type' put: contentType].
	aBlock value: resp.
	^self sendResponse: resp content: aString.