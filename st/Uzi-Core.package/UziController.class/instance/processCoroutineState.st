receiving
processCoroutineState
	| index pc fp pcLSB pcMSB stack stackSize val1 val2 val3 val4 |
	index := serial next.
	pcMSB := serial next.
	pcLSB := serial next.
	pc := (pcMSB << 8) bitOr: pcLSB.
	fp := serial next.
	stackSize := serial next.
	stack := FloatArray new: stackSize.
	1 to: stackSize do: [:i |
		val1 := serial next.
		val2 := serial next.
		val3 := serial next.
		val4 := serial next.
		stack basicAt: i put: (val1 << 24 bitOr: (val2 << 16 bitOr: (val3 << 8 bitOr: val4)))].
	debugger update: index pc: pc fp: fp stack: stack.	
	self triggerEvent: UziEvents debuggerUpdate