receiving
processPinValue
	| count timestamp update |
	timestamp := [| val1 val2 val3 val4 |
		val1 := serial next.
		val2 := serial next.
		val3 := serial next.
		val4 := serial next.
		val1 << 24 bitOr: (val2 << 16 bitOr: (val3 << 8 bitOr: val4))] value.
	count := serial next.

	update := false.
	count timesRepeat: [| val1 val2 pin value |
		val1 := serial next.
		val2 := serial next.
		pin := val1 >> 2.
		value := val2 bitOr: ((val1 bitAnd: 2r11) << 8).
		value := value / 1023.0.
		(self pinsReporting includes: pin)
			ifTrue: [(self getPin: pin) addValue: timestamp -> value.
				update := true]].
	update ifTrue: [self triggerEvent: UziEvents reportingChange]