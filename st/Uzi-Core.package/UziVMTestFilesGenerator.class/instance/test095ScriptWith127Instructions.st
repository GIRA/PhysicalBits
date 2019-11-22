tests
test095ScriptWith127Instructions
	| program |	
	program := Uzi program: [:p | p
		script: #test
		ticking: true
		delay: 0
		instructions: [:s | 
			31 timesRepeat: [s push: 13; prim: #toggle; push: 100; prim: #delayMs].
			s push: 1000; prim: #delayMs; resume: #test]].
	self writeFile: thisContext method selector
		program: program