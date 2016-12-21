responses
send404Response
	"Send a 404 not found response"

	^self send404Response: '<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p>The requested URL ',self rawUrl,' was not found on this server.</p></body></html>'.
