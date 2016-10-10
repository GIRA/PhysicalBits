instance creation
directoryNamed: aString
	^ self directory: (FileDirectory on: aString)