writing
flush
	self triggerEvent: #flushed with: buffer contents.
	buffer reset