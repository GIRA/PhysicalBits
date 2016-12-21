initialization
initializeStartingAt: aSymbol dependencies: aDictionary
	self initialize.
	parser := PPDelegateParser named: aSymbol.
	self productionNames keysAndValuesDo: [ :key :value |
		self instVarAt: key put: (PPDelegateParser named: value) ].
	dependencies := aDictionary