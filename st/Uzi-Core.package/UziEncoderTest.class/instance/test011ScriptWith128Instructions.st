tests
test011ScriptWith128Instructions
	| program |
	program := Uzi program: [:p | p
		script: #test
		ticking: true
		delay: 0
		instructions: [:s | 
			64 timesRepeat: [s push: 16; prim: #toggle]]].
	Uzi encode: program