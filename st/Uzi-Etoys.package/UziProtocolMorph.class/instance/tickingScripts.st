accessing
tickingScripts
	^ self scripts select: [:script | self isTicking: script]