accessing
isTicking: anUziScript 
	^ self isConnected and: [controller isTicking: anUziScript]