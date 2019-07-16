tests
test010MultipleScriptsWithDifferentTickingRates
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true 
		delay: 1000
		instructions: [:s | s
			push: 13;
			prim: #toggle];
		script: #pot
		ticking: true
		delay: 100
		instructions: [:s | s
			read: 15;
			write: 9]].
	self writeFile: 'Test010MultipleScriptsWithDifferentTickingRates'
		program: program