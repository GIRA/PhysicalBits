responses
send404Response: body do: aBlock
	"Send a 404 not found response"

	| resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: 404.
	resp headerAt: 'Content-Type' put: 'text/html; charset=utf-8'.
	aBlock value: resp.
	^self sendResponse: resp content: body.
