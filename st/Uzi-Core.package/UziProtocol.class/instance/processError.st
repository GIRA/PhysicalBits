receiving
processError
	| errorCode errorMsg |
	errorCode := serial next.
	errorMsg := errorCode 
		caseOf: {
			[1] -> ['STACK_OVERFLOW'].
			[2] -> ['STACK_UNDERFLOW'].
			[3] -> ['STACK_ACCESS_VIOLATION'].
			[4] -> ['OUT_OF_MEMORY'].
			[5] -> ['READER_TIMEOUT'].
			[6] -> ['DISCONNECT_ERROR'].
		} otherwise: ['UNKNOWN_ERROR'].
	Uzi logger
		cr;
		warning: '{1} detected. The program has been stopped.'
		format: { errorMsg };
		flush.
	errorCode = 6 
		ifTrue: [self fork: [self privateDisconnect] named: 'Disconnecting...']