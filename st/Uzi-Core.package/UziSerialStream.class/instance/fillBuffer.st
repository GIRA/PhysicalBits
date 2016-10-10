reading
fillBuffer
	| tmp |
	tmp := self readByteArray.
	[tmp isEmpty]
		whileTrue: [10 milliSeconds asDelay wait.
			tmp := self readByteArray].
	"Transcript show: tmp printString; cr."
	buffer := tmp readStream