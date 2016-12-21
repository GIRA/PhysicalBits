responses
send405Response: allowed content: body
	"Send a 405 method not allowed response"
	| resp |
	resp := self newResponse protocol: 'HTTP/1.1' code: 405.
	resp headerAt: 'Content-Type' put: 'text/html; charset=utf-8'.
	resp headerAt: 'allow' put: (String streamContents:[:s|
		allowed do:[:m| s nextPutAll: m] separatedBy:[s nextPut: $,]
	]).
	^self sendResponse: resp content: body.