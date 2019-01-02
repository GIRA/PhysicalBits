accessing
allPCs
	self flag: #Richo. "Referencing the 'program' inst var, which I'd like to remove"
	^ 0 to: program instructions size - 1