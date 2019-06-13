receiving
processError
	| errorCode errorMsg |
	errorCode := serial next.
	errorMsg := self errorMsgForCode: errorCode.
	Uzi logger
		cr;
		warning: '{1} detected. The program has been stopped.'
		format: { errorMsg };
		flush.
	errorCode = 6 
		ifTrue: [self fork: [self privateDisconnect] named: 'Disconnecting...']