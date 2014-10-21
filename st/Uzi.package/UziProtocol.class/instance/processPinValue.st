receiving
processPinValue
	| pin value val1 val2 |
	pin := self getArgument: serial next or: [^ self].
	val1 := self getArgument: serial next or: [^ self].
	val2 := self getArgument: serial next or: [^ self].
	value := val1 bitOr: (val2 << 7).
	pinValues at: pin put: value