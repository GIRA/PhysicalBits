testing
shouldStopMonitoring: now
	idleTimeout ifNil: [^ false].
	^ now - lastAccess > idleTimeout