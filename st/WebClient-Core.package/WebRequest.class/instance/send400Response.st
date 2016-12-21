responses
send400Response
	"Send a 400 bad request response"

	| message |
	message := '<html><head><title>400 Bad Request</title></head><body>',
		'<h1>Bad Request</h1>
		<p>The server encountered a request it did not understand.
		</body></html>'.
	
	self sendResponseCode: 400 
		content: message squeakToUtf8
		type: 'text/plain; charset=utf-8'
		close: true