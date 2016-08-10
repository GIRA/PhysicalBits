accessing
configurePins
	self flag: #Richo. "Hardcoded pin interval"
	2 to: 19 do: [:pinNumber |
		(protocol getPinMode: pinNumber) = self class OUTPUT
			ifTrue: [protocol setPin: pinNumber value: 0].
		protocol
			setPin: pinNumber mode: self class INPUT;
			setPin: pinNumber report: false]