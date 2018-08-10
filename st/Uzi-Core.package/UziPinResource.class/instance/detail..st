accessing
detail: req
	| detail |
	detail := super detail: req.
	(detail at: #elements) do: [:dict || pinNumber |
		pinNumber := dict at: #number.
		dict at: #history 
			put: ((self protocol getPinValues: pinNumber) copy
				collect: [:kvp | Dictionary new
					at: #timestamp put: kvp key;
					at: #value put: kvp value;
					yourself] 
				as: Array)].
	^ detail