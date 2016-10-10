stepping
startSteppingProcess
	process ifNotNil: [^ self].
	process := [[self step.
			self stepTime milliSeconds asDelay wait] repeat]
		forkNamed: ('Uzi - Step ({1})' format: { self hash asString })