receiving
errorMsgForCode: errorCode
	^ errorCode 
		caseOf: {
			[0] -> ['NO_ERROR'].
			[1] -> ['STACK_OVERFLOW'].
			[2] -> ['STACK_UNDERFLOW'].
			[3] -> ['STACK_ACCESS_VIOLATION'].
			[4] -> ['OUT_OF_MEMORY'].
			[5] -> ['READER_TIMEOUT'].
			[6] -> ['DISCONNECT_ERROR'].
		} otherwise: ['UNKNOWN_ERROR'].