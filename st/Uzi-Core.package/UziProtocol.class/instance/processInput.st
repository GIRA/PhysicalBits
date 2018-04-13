receiving
processInput
	| cmd |
	cmd := serial next.
	cmd caseOf: {
		[self class rsError] -> [self processError].
		[self class rsPinValue] -> [self processPinValue].
		[self class rsProfile] -> [self processProfile].
		[self class rsGlobalValue] -> [self processGlobalValue].
		[self class rsTrace] -> [self processTrace].
		[self class rsCoroutineState] -> [self processCoroutineState].
		[self class rsTickingScripts] -> [self processTickingScripts].
		[self class rsFreeRAM] -> [self processFreeRAM]
	} otherwise: [UziTranscript 
		log: 'Uzi - Invalid response code: {1}' 
		format: { cmd }]