accessing
outputAt: id put: value
	^ (self clientAt: id) at: #output put: value