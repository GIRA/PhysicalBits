start/stop
stop
	self isStarted ifFalse: [^ self].
	process terminate.
	process := nil