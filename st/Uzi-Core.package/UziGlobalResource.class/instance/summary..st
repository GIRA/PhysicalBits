accessing
summary: req
	^ (super summary: req)
		at: #available
		put: (self controller availableGlobals
			collect: [:globalName | 
				Dictionary new
					at: #name put: globalName;
					at: #reporting put: (self controller globalsReporting includes: globalName);
					yourself]
			as: Array);
		at: #elements 
		put: (self controller globals 
			collect: [:global |
				Dictionary new 
					at: #name put: global name;
					at: #value put: (UziJSON createJSONObject: global value);
					yourself]
			as: Array);
		yourself