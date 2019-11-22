tests
test096ScriptWith128Instructions
	| program |	
	program := Uzi program: [:p | p
		script: #test
		ticking: true
		delay: 0
		instructions: [:s | 
			32 timesRepeat: [s push: 13; prim: #toggle; push: 100; prim: #delayMs]]].
	self writeFile: thisContext method selector
		program: program