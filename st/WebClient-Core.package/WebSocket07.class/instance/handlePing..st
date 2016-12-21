control requests
handlePing: payload

	self handle: 'ping' 
		arguments: (Array with: payload with: self)
		ifAbsent:[self pong: payload].
