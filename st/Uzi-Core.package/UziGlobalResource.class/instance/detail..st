accessing
detail: req
	| start globals |
	start := (req fields at: 'start' ifAbsent: 0) asInteger.
	globals := ((req fields at: 'globals' ifAbsent: '') subStrings: ',')
		collect: [:each | each asInteger].
	^ (super detail: req)
		at: #reporting
		put: (globals
			collect: [:globalNumber || global |
				global := self protocol getGlobal: globalNumber.
				Dictionary new 
					at: #name put: global name;
					at: #number put: globalNumber;
					at: #history 
					put: ((global history
						reject: [:kvp | kvp key <= start])
						collect: [:kvp | Dictionary new
							at: #timestamp put: kvp key;
							at: #value put: (UziJSON createJSONObject: kvp value);
							yourself] 
						as: Array);
					yourself]
			as: Array);
		yourself 