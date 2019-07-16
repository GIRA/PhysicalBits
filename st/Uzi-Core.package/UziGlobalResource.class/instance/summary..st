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
		at: #elements 
		put: (self controller globals 
			collect: [:global |
				Dictionary new 
					at: #name put: global name;
					at: #number put: global number;
					at: #value put: (UziJSON createJSONObject: global value);
					yourself]
			as: Array);
		yourself