tests
test013YieldInstructionResumesOnNextTick
	| program |
	program := Uzi program: [:p | p
		script: #yieldTest
		ticking: true
		delay: 1000
		instructions: [:s | s
			turnOn: 12;
			prim: #yield;
			turnOff: 12]].
	self writeFile: 'Test013YieldInstructionResumesOnNextTick'
		program: program