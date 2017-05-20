accessing
currentScript
	pc ifNotNil: [| start |
			start := 0.
			protocol scripts do: [:script || stop |
				stop := start + script instructions size. 
				(pc >= start and: [pc < stop])
					ifTrue: [^ script]
					ifFalse: [start := stop]]].
	^ nil