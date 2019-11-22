tests
test097ScriptWith512Instructions
	| program |	
	program := Uzi program: [:p | p
		script: #test
		ticking: true
		delay: 0
		instructions: [:s | 
			128 timesRepeat: [s push: 13; prim: #toggle; push: 100; prim: #delayMs]]].
	self writeFile: thisContext method selector
		program: program