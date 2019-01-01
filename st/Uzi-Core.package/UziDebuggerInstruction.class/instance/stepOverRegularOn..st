stepping
stepOverRegularOn: debugger
	^ self nextInstruction
		ifNil: [self stepOverReturnOn: debugger]
		ifNotNil: [:next | { next start }]