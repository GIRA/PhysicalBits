receiving
processInput
	| cmd |
	serial ifNil: [^ self].
	[cmd := serial next]
		on: ConnectionTimedOut
		do: ["Nothing. Timeouts are acceptable here"
			^ self].
	[cmd caseOf: {
		[self class msgInError] -> [self processError].
		[self class msgInPinValue] -> [self processPinValue].
		[self class msgInProfile] -> [self processProfile].
		[self class msgInGlobalValue] -> [self processGlobalValue].
		[self class msgInTrace] -> [self processTrace].
		[self class msgInCoroutineState] -> [self processCoroutineState].
		[self class msgInTickingScripts] -> [self processTickingScripts].
		[self class msgInFreeRam] -> [self processFreeRAM].
		[self class msgInSerialTunnel] -> [self processSerialTunnel]
	} otherwise: [Uzi logger 
			warning: 'Uzi - Invalid response code: {1}' 
			format: { cmd };
			flush]]
		on: ConnectionTimedOut
		do: [Uzi logger error: 'Connection timeout!'; flush.
			self fork: [self privateDisconnect] named: 'Disconnecting...']