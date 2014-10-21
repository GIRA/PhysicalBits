reading
fillBuffer
	| tmp |
	tmp := port readByteArray.
	[tmp isEmpty]
		whileTrue: [10 milliSeconds asDelay wait.
			tmp := port readByteArray].
	"Transcript show: tmp printString; cr."
	buffer := tmp readStream