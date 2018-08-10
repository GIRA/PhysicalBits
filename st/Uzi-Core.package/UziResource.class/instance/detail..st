accessing
detail: req
	^ (super detail: req)
		at: #isConnected put: self protocol isConnected;
		at: #portName put: self protocol portName;
		yourself 