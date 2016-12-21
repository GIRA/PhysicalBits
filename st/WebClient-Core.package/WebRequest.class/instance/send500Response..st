responses
send500Response: errReport
	"Send a 500 internal server error response"

	| errLog |
	errLog := '<html><head><title>500 Internal Server Error</title></head><body>',
		'<h1>Internal Server Error</h1><pre>',
		(errReport  copyReplaceAll: String cr with: String crlf),
		'</pre></body></html>'.

	self sendResponseCode: 500
				content: errLog squeakToUtf8
				type: 'text/html; charset=utf-8'
				close: true.
