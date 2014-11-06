start/stop
start
	self isStarted ifTrue: [^ self].
	process := [[self step.
		(Delay forMilliseconds: self stepTime) wait] repeat]
			forkNamed: self asString