accessing
detail
	^ super detail
		at: #isConnected put: protocol isConnected;
		at: #portName put: protocol portName;
		yourself 