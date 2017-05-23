receiving
processError
	| scriptIndex errorCode errorMsg script |
	scriptIndex := serial next.
	script := currentProgram scripts at: scriptIndex + 1 ifAbsent: ['UNKNOWN'].
	errorCode := serial next.
	errorMsg := errorCode 
		caseOf: {
			[1] -> ['STACK_OVERFLOW'].
			[2] -> ['STACK_UNDERFLOW'].
			[3] -> ['STACK_ACCESS_VIOLATION']
		} 
		otherwise: ['UNKNOWN_ERROR'].
	WorldState addDeferredUIMessage: [
		Transcript 
			show: ('{1} detected on {2}! The script has been stopped.'
				format: { errorMsg . script });
			cr]