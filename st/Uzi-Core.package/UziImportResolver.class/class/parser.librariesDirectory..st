instance creation
parser: anUziParser librariesDirectory: librariesDirectory
	^ self basicNew 
		initializeWithParser: anUziParser
		librariesDirectory: librariesDirectory 