accessing
scriptName
	^ index ifNil: 'Unknown?' ifNotNil: [:i | (self program scripts at: i + 1) scriptName]