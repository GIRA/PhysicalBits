accessing
detail
	^ super detail
		at: #isConnected put: self protocol isConnected;
		at: #portName put: self protocol portName;
		yourself 