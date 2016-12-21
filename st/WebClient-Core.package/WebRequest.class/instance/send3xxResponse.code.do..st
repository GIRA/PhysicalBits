responses
send3xxResponse: location code: statusCode do: aBlock
	"Send a 3xx redirect response"

	| resp url |
	"If the redirect is not to an absolute url, prefix it with the host if we have one"
	url := location.
	(location findString: '://') > 0 ifFalse:[
		(self headerAt: 'host') ifNotEmpty:[:host| url := 'http://', host, location]].

	resp := self newResponse protocol: 'HTTP/1.1' code: statusCode.
	resp headerAt: 'Location' put: url.
	aBlock value: resp.
	^self sendResponse: resp content: ''.
