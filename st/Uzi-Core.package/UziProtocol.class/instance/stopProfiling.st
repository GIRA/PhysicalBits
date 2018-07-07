profiling
stopProfiling
	^ self send: { self class msgOutProfile . 0 } 