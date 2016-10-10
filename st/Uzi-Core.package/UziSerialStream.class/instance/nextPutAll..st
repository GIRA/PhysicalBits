writing
nextPutAll: aByteArray
	^ [port nextPutAll: aByteArray]
		on: Error
		do: [:ign | -1]