stepping
stopSteppingProcess
	process ifNil: [^ self].
	process terminate.
	process := nil