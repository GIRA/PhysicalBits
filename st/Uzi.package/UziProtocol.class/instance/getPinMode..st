accessing-pins
getPinMode: pinNumber 
	^ pinModes
		at: pinNumber
		ifAbsent: [0]