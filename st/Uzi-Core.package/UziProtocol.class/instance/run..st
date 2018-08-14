installing
run: anUziProgram
	currentProgram := anUziProgram.
	globals := Dictionary new.
	[^ self send: { self class msgOutSetProgram }, (Uzi encode: anUziProgram)]
		ensure: [self triggerEvent: UziProtocolEvents runProgram]