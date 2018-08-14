accessing
detail: req
	| start pins |
	start := (req fields at: 'start' ifAbsent: 0) asInteger.
	pins := ((req fields at: 'pins' ifAbsent: '') subStrings: ',')
		collect: [:each | each asInteger].
	^ (super detail: req)
		at: #elements 
		put: (pins collect: [:pinNumber | Dictionary new 
			at: #number put: pinNumber;
			at: #history 
			put: (((self protocol getPinValues: pinNumber) copy
				reject: [:kvp | kvp key <= start])
				collect: [:kvp | Dictionary new
					at: #timestamp put: kvp key;
					at: #value put: kvp value;
					yourself] 
				as: Array);
				yourself]
			as: Array);
		yourself 