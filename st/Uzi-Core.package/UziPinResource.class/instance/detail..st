accessing
detail: req
	| start pins |
	start := (req fields at: 'start' ifAbsent: 0) asInteger.
	pins := ((req fields at: 'pins' ifAbsent: '') subStrings: ',')
		collect: [:each | each asInteger].
	^ (super detail: req)
		at: #elements 
		put: (pins 
			collect: [:pinNumber || pin |
				pin := self protocol getPin: pinNumber.
				Dictionary new 
					at: #name put: pin name;
					at: #number put: pinNumber;
					at: #history 
					put: ((pin history
						reject: [:kvp | kvp key <= start])
						collect: [:kvp | Dictionary new
							at: #timestamp put: kvp key;
							at: #value put: (UziJSON createObject: kvp value);
							yourself] 
						as: Array);
					yourself]
			as: Array);
		yourself 