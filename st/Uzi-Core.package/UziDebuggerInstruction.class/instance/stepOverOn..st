stepping
stepOverOn: debugger
	self isTrivial ifTrue: [^ self stepOverTrivialOn: debugger].
	self isCall ifTrue: [^ self stepOverCallOn: debugger].
	self isReturn ifTrue: [^ self stepOverReturnOn: debugger].
	self isBranch ifTrue: [^ self stepOverBranchOn: debugger].
	^ self stepOverRegularOn: debugger