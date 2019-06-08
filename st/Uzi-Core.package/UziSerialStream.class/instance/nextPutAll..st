writing
nextPutAll: aByteArray
	self class debugEnabled
		ifTrue: [Uzi logger log: 'OUT: {1}' format: { aByteArray printString }; flush].
	^ [port nextPutAll: aByteArray]
		on: Error
		do: [:ign | -1]