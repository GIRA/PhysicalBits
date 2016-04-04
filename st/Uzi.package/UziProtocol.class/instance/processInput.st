receiving
processInput
	| in cmd |
	[in := serial next]
		doWhileFalse: [self isCommand: in].
	cmd := self getCommand: in.
	cmd caseOf: {
		[self class rsError] -> [self processError].
		[self class rsPinValue] -> [self processPinValue].
		[self class rsProfile] -> [self processProfile]
	} otherwise: [Transcript 
		show: 'Uzi - Invalid response code: '; 
		show: cmd; 
		cr]