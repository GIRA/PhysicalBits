accessing
isTicking: anUziScript 
	^ scriptWatchers at: anUziScript 
		ifPresent: [:w | w isRunning]
		ifAbsent: [false]