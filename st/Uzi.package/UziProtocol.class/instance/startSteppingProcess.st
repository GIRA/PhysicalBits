stepping
startSteppingProcess
	process ifNotNil: [^ self].
	process := [[self step.
			self stepTime milliSeconds asDelay wait] repeat]
		forkNamed: self asString