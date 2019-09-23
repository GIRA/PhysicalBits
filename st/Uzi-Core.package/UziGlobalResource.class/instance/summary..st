accessing
summary: req
	^ (super summary: req)
		at: #available
		put: ((self controller availableGlobals
			reject: [:globalName | globalName includes: $.])
			collect: [:globalName | 
				Dictionary new
					at: #name put: globalName;
					at: #reporting put: (self controller globalsReporting includes: globalName);
					yourself]
			as: Array);
		at: #elements 
		put: ((self controller globals 
			reject: [:global | global name includes: $.])
			collect: [:global |
				Dictionary new 
					at: #name put: global name;
					at: #value put: (UziJSON createJSONObject: global value);
					yourself]
			as: Array);
		yourself