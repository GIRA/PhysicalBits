installing
run: anUziProgram
	runningProgram := anUziProgram.
	globals := globals select: [:g | runningProgram includesGlobalNamed: g name].
	debugger reset.
	[^ self send: { self class msgOutSetProgram }, (Uzi encode: anUziProgram)]
		ensure: [self triggerEvent: UziProtocolEvents programUpdate]