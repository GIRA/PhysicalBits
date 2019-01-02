stepping
stepOverRegularOn: debugger
	^ self nextInstruction
		ifNil: [self stepOverReturnOn: debugger]
		ifNotNil: [:next | next isTrivial 
			ifTrue: [next stepOverOn: debugger]
			ifFalse: [{ next start }]]