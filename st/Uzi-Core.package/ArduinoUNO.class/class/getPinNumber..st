accessing
getPinNumber: pinName 
	^ pinsByName at: pinName ifAbsent: [nil]