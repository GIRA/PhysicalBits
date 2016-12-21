*REST-Core
send400Response: msg
	self sendResponseCode: 400 
		content: msg squeakToUtf8
		type: 'text/plain; charset=utf-8'
		close: true