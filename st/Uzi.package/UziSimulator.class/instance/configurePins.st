accessing
configurePins
	2 to: 19 do: [:pinNumber || output input |
		output := program outputs includes: pinNumber.
		input := program inputs includes: pinNumber.
		(pinNumber = 9 and: [input])
			ifTrue: [self halt].
		protocol 
			setPin: pinNumber
			mode: (output ifTrue: [1] ifFalse: [0]);
			setPin: pinNumber
			report: input]