executing
execute
	| now |
	script isTicking ifFalse: [^ self].
	now := Time millisecondClockValue.
	now - lastTime < script delay ifTrue: [^ self].
	lastTime := now.
	
	ip := 0.
	stack := UziStack new.
	[ip < script bytecodes size] whileTrue: [
		self executeBytecode: self nextBytecode.
		stack overflow
			ifTrue: [self flag: #Richo. "Notify overflow"
				ip := script bytecodes size]]