accessing
getPinName: pinNumber
	^ pinsByNumber at: pinNumber ifAbsent: [nil]