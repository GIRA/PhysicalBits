accessing
configurePins
	2 to: 19 do: [:pinNumber |
		protocol 
			setPin: pinNumber
			mode: ((program inputs includes: pinNumber)
				ifTrue: [0] ifFalse: [1])]