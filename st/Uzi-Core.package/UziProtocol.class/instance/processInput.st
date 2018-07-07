receiving
processInput
	| cmd |
	cmd := serial next.
	cmd caseOf: {
		[self class msgInError] -> [self processError].
		[self class msgInPinValue] -> [self processPinValue].
		[self class msgInProfile] -> [self processProfile].
		[self class msgInGlobalValue] -> [self processGlobalValue].
		[self class msgInTrace] -> [self processTrace].
		[self class msgInCoroutineState] -> [self processCoroutineState].
		[self class msgInTickingScripts] -> [self processTickingScripts].
		[self class msgInFreeRam] -> [self processFreeRAM].
		[self class msgInSerialTunnel] -> [self processSerialTunnel]
	} otherwise: [UziTranscript 
		log: 'Uzi - Invalid response code: {1}' 
		format: { cmd }]