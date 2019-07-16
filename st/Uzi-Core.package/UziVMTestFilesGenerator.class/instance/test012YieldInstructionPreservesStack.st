tests
test012YieldInstructionPreservesStack
	| program |
	program := Uzi program: [:p | p
		script: #yieldTest
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #toggle;
			push: 13;
			prim: #yield;
			prim: #toggle;
			push: 12;
			push: 1;
			prim: #yield;
			prim: #add;
			prim: #toggle]].
	self writeFile: 'Test012YieldInstructionPreservesStack'
		program: program