instance creation
parser: anUziParser librariesPath: pathString
	^ self basicNew 
		initializeWithParser: anUziParser
		librariesDirectory: (FileDirectory on: pathString)