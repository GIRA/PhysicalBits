executing
execute
	| now |
	script isTicking ifFalse: [^ self].
	now := Time millisecondClockValue.
	now - lastTime < script delay ifTrue: [^ self].
	lastTime := now.
	
	ip := 0.
	stack := UziStack new.
	next := self nextBytecode.
	[next isEnd] whileFalse: [
		self executeBytecode: next.
		stack overflow
			ifTrue: [next := UziEndBytecode new]
			ifFalse: [next := self nextBytecode]]