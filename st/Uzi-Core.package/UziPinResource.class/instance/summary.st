accessing
summary 
	^ super summary
		at: #elements put: (self protocol pinsReporting 
			collect: [:pinNumber | Dictionary new 
				at: #number put: pinNumber;
				at: #value put: (self protocol getPinValue: pinNumber);
				yourself]
			as: Array);
		yourself 