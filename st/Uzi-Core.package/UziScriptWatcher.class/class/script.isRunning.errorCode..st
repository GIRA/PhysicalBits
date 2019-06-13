instance creation
script: anUziScript isRunning: aBoolean errorCode: anInteger 
	^ self basicNew
		initializeWithScript: anUziScript
		isRunning: aBoolean
		errorCode: anInteger