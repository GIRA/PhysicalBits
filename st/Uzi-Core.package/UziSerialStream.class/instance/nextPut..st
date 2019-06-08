writing
nextPut: aSmallInteger
	self class debugEnabled 
		ifTrue: [Uzi logger log: 'OUT: {1}' format: { aSmallInteger }; flush].
	^ [port nextPut: aSmallInteger]
		on: Error
		do: [:ign | -1]