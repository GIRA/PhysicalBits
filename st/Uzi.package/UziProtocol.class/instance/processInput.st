receiving
processInput
	| cmd |
	cmd := serial next.
	cmd caseOf: {
		[self class rsError] -> [self processError].
		[self class rsPinValue] -> [self processPinValue].
		[self class rsProfile] -> [self processProfile].
		[self class rsGlobalValue] -> [self processGlobalValue]
	} otherwise: [Transcript 
		show: 'Uzi - Invalid response code: '; 
		show: cmd; 
		cr]