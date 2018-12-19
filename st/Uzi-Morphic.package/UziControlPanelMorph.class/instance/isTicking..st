accessing
isTicking: anUziScript 
	^ self isConnected and: [protocol isTicking: anUziScript]