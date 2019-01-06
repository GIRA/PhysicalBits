installing
run: anUziProgram
	currentProgram := anUziProgram.
	globals := globals select: [:g | currentProgram includesGlobalNamed: g name].
	debugger reset.
	[^ self send: { self class msgOutSetProgram }, (Uzi encode: anUziProgram)]
		ensure: [self triggerEvent: UziProtocolEvents runProgram]