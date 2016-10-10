instance creation
value: aPositiveNumber scale: aSymbol
	aPositiveNumber <= 0 
		ifTrue: [UziInvalidTickingRateError signal: aPositiveNumber].
	^ self basicNew 
		initializeWithValue: aPositiveNumber 
		scale: aSymbol 