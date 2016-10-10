accessing-pins
getPinValue: pinNumber 
	^ pinValues at: pinNumber ifAbsent: [0]