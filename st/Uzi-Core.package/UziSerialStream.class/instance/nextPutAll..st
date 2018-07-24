writing
nextPutAll: aByteArray
	self class debugEnabled
		ifTrue: [UziTranscript log: 'OUT: {1}' format: { aByteArray printString }].
	^ [port nextPutAll: aByteArray]
		on: Error
		do: [:ign | -1]