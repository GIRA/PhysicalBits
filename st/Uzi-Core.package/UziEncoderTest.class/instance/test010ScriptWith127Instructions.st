tests
test010ScriptWith127Instructions
	| program |
	program := Uzi program: [:p | p
		script: #test
		ticking: true
		delay: 0
		instructions: [:s | 
			63 timesRepeat: [s push: 16; prim: #toggle].
			s stop: #test]].
	Uzi encode: program