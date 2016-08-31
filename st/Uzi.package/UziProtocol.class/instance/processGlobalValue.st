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
		val := Float fromIEEE32Bit: (val1 << 24 bitOr: (val2 << 16 bitOr: (val3 << 8 bitOr: val4))).
		globals at: index put: val]