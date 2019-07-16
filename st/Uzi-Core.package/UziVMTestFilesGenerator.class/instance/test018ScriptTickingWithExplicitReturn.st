tests
test018ScriptTickingWithExplicitReturn
	| program |
	program := Uzi program: [:p | p	
		script: #incr
		ticking: true
		delay: 1
		instructions: [:s | s
			push: 13;
			prim: #toggle;
			push: #counter;
			push: 1;
			prim: #add;
			pop: #counter;
			push: #counter;
			prim: #retv;
			push: 11;
			prim: #toggle]].
	self writeFile: 'Test018ScriptTickingWithExplicitReturn'
		program: program