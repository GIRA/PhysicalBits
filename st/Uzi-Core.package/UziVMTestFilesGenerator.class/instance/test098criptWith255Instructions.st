tests
test098criptWith255Instructions
	| program |	
	program := Uzi program: [:p | p
		script: #test
		ticking: true
		delay: 0
		instructions: [:s | 
			63 timesRepeat: [s push: 13; prim: #toggle; push: 1; prim: #delayMs].
			s push: 13; prim: #toggle; resume: #test]].
	self writeFile: thisContext method selector
		program: program