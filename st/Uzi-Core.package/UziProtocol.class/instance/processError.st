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
			[3] -> ['STACK_ACCESS_VIOLATION']
		} otherwise: ['UNKNOWN_ERROR'].
	WorldState addDeferredUIMessage: [
		Transcript 
			cr;
			show: ('{1} detected on script @{2}. The script has been stopped.'
				format: { errorMsg . script ifNotNil: [script name] });
			cr]