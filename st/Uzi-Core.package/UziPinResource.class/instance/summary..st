accessing
summary: req
	^ (super summary: req)
		at: #elements 
		put: (self controller pins 
			collect: [:pin |
				Dictionary new 
					at: #name put: pin name;
					at: #number put: pin number;
					at: #value 
					put: (UziJSON createJSONObject: pin value);
					yourself]
			as: Array);
		yourself 