receiving
processGlobalValue
	| val val1 val2 val3 val4 count index |
	count := serial next.
	count timesRepeat: [
		index := serial next.
		val1 := serial next.
		val2 := serial next.
		val3 := serial next.
		val4 := serial next.
		val := (FloatArray new: 1) 
			basicAt: 1 
			put: (val1 << 24 bitOr: (val2 << 16 bitOr: (val3 << 8 bitOr: val4)));
			at: 1.
		(globals includesKey: index)
			ifTrue: [globals at: index put: val]]