installing
run: anUziProgram
	currentProgram := anUziProgram.
	globals := globals select: [:g | currentProgram includesGlobalNamed: g name].
	debugger close.
	[^ self send: { self class msgOutSetProgram }, (Uzi encode: anUziProgram)]
		ensure: [self triggerEvent: UziProtocolEvents runProgram]