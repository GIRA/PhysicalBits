requests
process: aWebRequest path: path
	path ifEmpty: [self processRequest: aWebRequest. ^ true].
	^ self at: path first
		ifPresent: [:child | child process: aWebRequest path: path allButFirst]
		ifAbsent: false