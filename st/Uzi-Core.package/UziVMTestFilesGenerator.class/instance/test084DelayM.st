tests
test084DelayM
	| program |
	program := Uzi program: [:p | p
		script: #delayS
		ticking: true
		delay: 100
		instructions: [:s | s
			turnOn: 13;
			push: 1;
			prim: #delayM;
			turnOff: 13]].
	self writeFile: thisContext method selector
		program: program