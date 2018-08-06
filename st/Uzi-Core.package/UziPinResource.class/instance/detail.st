accessing
detail 
	| detail |
	detail := super detail.
	(detail at: #elements) do: [:dict || pinNumber |
		pinNumber := dict at: #number.
		dict at: #history 
			put: ((self protocol getPinValues: pinNumber) copy
				collect: [:kvp | Dictionary new
					at: #timestamp put: kvp key asString;
					at: #value put: kvp value;
					yourself] 
				as: Array)].
	^ detail