accessing
summary: req
	^ (super summary: req)
		at: #available
		put: (self protocol variables
			collect: [:var | Dictionary new
				at: #name put: var;
				at: #number put: (self protocol getGlobalNumber: var);
				yourself]
			as: Array);
		yourself