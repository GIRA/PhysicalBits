accessing
scriptForPC: anInteger
	| start |
	start := 0.
	protocol scripts do: [:script || stop |
		stop := start + script instructions size. 
		(anInteger >= start and: [anInteger < stop])
			ifTrue: [^ script]
			ifFalse: [start := stop]].
	^ nil