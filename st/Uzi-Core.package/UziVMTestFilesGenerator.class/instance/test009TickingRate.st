tests
test009TickingRate
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true 
		delay: 1000
		instructions: [:s | s
			push: 13;
			prim: #toggle]].
	self writeFile: 'Test009TickingRate'
		program: program