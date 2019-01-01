stepping
stepIntoCallOn: debugger
	| instr target |
	instr := (self callInstruction: debugger pc) 
		ifNil: [^ self stepOverRegularOn: debugger].
	target := self program 
		scriptNamed: instr argument
		ifAbsent: [^ self stepOverRegularOn: debugger].
	^ { target start }