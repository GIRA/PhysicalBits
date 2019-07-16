tests
test011YieldInstruction
	| program |
	program := Uzi program: [:p | p
		script: #yieldTest
		ticking: true
		delay: 0
		instructions: [:s | s
			turnOn: 13;
			prim: #yield;
			turnOff: 13]].
	self writeFile: 'Test011YieldInstruction'
		program: program