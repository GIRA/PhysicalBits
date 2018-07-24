writing
nextPut: aSmallInteger
	self class debugEnabled 
		ifTrue: [UziTranscript log: 'OUT: {1}' format: { aSmallInteger }].
	^ [port nextPut: aSmallInteger]
		on: Error
		do: [:ign | -1]