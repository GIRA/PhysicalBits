accessing
summary: req
	^ (super summary: req)
		at: #available
		put: (self controller variables
			collect: [:var | Dictionary new
				at: #name put: var;
				at: #number put: (self controller getGlobalNumber: var);
				yourself]
			as: Array);
		yourself