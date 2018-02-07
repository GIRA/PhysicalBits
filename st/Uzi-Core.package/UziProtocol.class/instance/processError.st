receiving
processError
	| scriptIndex errorCode errorMsg script |
	scriptIndex := serial next.
	script := currentProgram ifNotNil: [:p | p scripts at: scriptIndex + 1 ifAbsent: [nil]].
	errorCode := serial next.
	errorMsg := errorCode 
		caseOf: {
			[1] -> ['STACK_OVERFLOW'].
			[2] -> ['STACK_UNDERFLOW'].
			[3] -> ['STACK_ACCESS_VIOLATION'].
			[4] -> ['OUT_OF_MEMORY'].
			[5] -> ['READER_TIMEOUT']
		} otherwise: ['UNKNOWN_ERROR'].
	WorldState addDeferredUIMessage: [| msg |
		msg := script 
			ifNil: ['{1} detected. The program has been stopped.'
				format: { errorMsg }]
			ifNotNil: ['{1} detected on script "{2}". The script has been stopped.'
				format: { errorMsg . script name }].
		Transcript cr; show: msg; cr]