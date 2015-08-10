writing
nextPut: aSmallInteger 
	^ [port nextPut: aSmallInteger]
		on: Error
		do: [:ign | -1]