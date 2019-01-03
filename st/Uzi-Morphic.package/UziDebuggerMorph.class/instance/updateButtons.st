updating
updateButtons
	debugger isHalted
		ifTrue: [self unlockButtons]
		ifFalse: [self lockButtons]