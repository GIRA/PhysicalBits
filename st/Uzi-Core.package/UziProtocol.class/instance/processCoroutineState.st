receiving
processCoroutineState
	| index pc pcLSB pcMSB stack stackSize val val1 val2 val3 val4 |
	index := serial next.
	pcMSB := serial next.
	pcLSB := serial next.
	pc := (pcMSB << 8) bitOr: pcLSB.
	stackSize := serial next.
	stack := OrderedCollection new.
	stackSize timesRepeat: [
		val1 := serial next.
		val2 := serial next.
		val3 := serial next.
		val4 := serial next.
		val := Float fromIEEE32Bit: (val1 << 24 bitOr: (val2 << 16 bitOr: (val3 << 8 bitOr: val4))).
		stack add: val].
	self triggerEvent: #coroutineUpdate
		withArguments: { index . pc . stack }