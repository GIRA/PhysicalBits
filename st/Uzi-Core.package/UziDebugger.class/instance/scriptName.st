accessing
scriptName
	^ index ifNil: 'Unknown?' ifNotNil: [:i | (program scripts at: i + 1) name]