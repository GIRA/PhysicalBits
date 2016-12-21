control requests
handlePong: payload

	self handle: 'pong' arguments: (Array with: payload with: self).