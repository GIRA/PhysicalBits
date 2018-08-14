testing
shouldStopMonitoring: now
	^ now - lastAccess > 1000