writing
flush
	self triggerEvent: #flushed with: buffer asArray.
	buffer removeAll