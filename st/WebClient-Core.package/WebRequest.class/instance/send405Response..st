responses
send405Response: allowed
	"Send a 405 method not allowed response"

	^self send405Response: allowed content: '<html><head><title>405 Method Not Allowed</title></head><body><h1>405 Method Not Allowed</h1><p>The requested method ',self method,' is not allowed for the URL', self rawUrl, '</p></body></html>'.