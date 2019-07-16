tests
test042EmptyScriptShouldNotCrashTheVM
	| program |
	program := Uzi program: [:p | p
		script: #empty
		ticking: true
		delay: 0
		instructions: [:s |]].
	self writeFile: 'Test042EmptyScriptShouldNotCrashTheVM'
		program: program