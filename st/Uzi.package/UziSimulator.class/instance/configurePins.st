accessing
configurePins
	self flag: #Richo. "Hardcoded pin interval"
	2 to: 19 do: [:pinNumber |
		(protocol getPinValue: pinNumber) = 1
			ifTrue: [protocol setPin: pinNumber value: 0].
		protocol
			setPin: pinNumber mode: 0;
			setPin: pinNumber report: false]