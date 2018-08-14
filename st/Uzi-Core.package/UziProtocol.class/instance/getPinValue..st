accessing-pins
getPinValue: pinNumber 
	^ (self getPinValues: pinNumber) 
		ifEmpty: [nil]
		ifNotEmpty: [:list | list last value]