tests
test083DelayS
	| program |
	program := Uzi program: [:p | p
		script: #delayS
		ticking: true
		delay: 100
		instructions: [:s | s
			turnOn: 13;
			push: 1;
			prim: #delayS;
			turnOff: 13]].
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)