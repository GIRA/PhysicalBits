accessing-pins
getPinValue: pinNumber 
	^ (self getPinValues: pinNumber) 
		ifEmpty: [0]
		ifNotEmpty: [:list | list last value]