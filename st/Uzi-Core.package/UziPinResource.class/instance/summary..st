accessing
summary: req
	^ (super summary: req)
		at: #available
		put: (self controller availablePins
			collect: [:pinName |
				Dictionary new
					at: #name put: pinName;
					at: #reporting put: (self controller pinsReporting includes: pinName);
					yourself]);
		at: #elements 
		put: (self controller pins 
			collect: [:pin |
				Dictionary new 
					at: #name put: pin name;
					at: #value put: (UziJSON createJSONObject: pin value);
					yourself]
			as: Array);
		yourself 