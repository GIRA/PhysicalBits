tests
test017ScriptCallWithoutParametersWithReturnValueAndExplicitReturn
	| program |
	program := Uzi program: [:p | p
		registerGlobal: #counter value: 5;
		script: #incr
		ticking: false
		delay: 0
		instructions: [:s | s
			push: #counter;
			push: 1;
			prim: #add;
			pop: #counter;
			push: #counter;
			prim: #retv;
			push: 13;
			prim: #toggle];
		script: #loop
		ticking: true
		delay: 100
		instructions: [:s | s
			call: #incr;
			push: 100;
			prim: #divide;
			write: 11]].
	self writeFile: 'Test017ScriptCallWithoutParametersWithReturnValueAndExplicitReturn'
		program: program