instance creation
parser: anUziParser librariesPath: pathString
	^ self parser: anUziParser
		librariesDirectory: (FileDirectory on: pathString)