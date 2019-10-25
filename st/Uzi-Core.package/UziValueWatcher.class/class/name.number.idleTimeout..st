instance creation
name: aString number: anInteger idleTimeout: idleTimeout
	^ self basicNew 
		initializeWithName: aString 
		number: anInteger
		idleTimeout: idleTimeout