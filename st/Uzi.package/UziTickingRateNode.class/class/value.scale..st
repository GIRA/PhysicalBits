instance creation
value: aPositiveNumber scale: aSymbol
	aPositiveNumber <= 0 
		ifTrue: [InvalidTickingRateError signal].
	^ self basicNew 
		initializeWithValue: aPositiveNumber 
		scale: aSymbol 