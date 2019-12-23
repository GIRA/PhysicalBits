reading
fillBuffer
	| tmp start |
	start := Time millisecondClockValue.
	tmp := self readByteArray.
	(tmp notEmpty and: [self class debugEnabled]) 
		ifTrue: [Uzi logger log: 'BUF: %1' format: { tmp printString }; flush].
	[tmp isEmpty] whileTrue: [
		(Time millisecondsSince: start) > timeout
			ifTrue: [ConnectionTimedOut signal: 'Trying to fill the Uzi buffer'].
		10 milliSeconds asDelay wait.
		tmp := self readByteArray.
		(tmp notEmpty and: [self class debugEnabled]) 
			ifTrue: [Uzi logger log: 'BUF: %1' format: { tmp printString }; flush]].
	buffer := tmp readStream