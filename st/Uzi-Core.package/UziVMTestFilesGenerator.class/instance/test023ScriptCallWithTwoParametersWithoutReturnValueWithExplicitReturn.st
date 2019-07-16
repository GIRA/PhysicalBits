tests
test023ScriptCallWithTwoParametersWithoutReturnValueWithExplicitReturn
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 1000
		instructions: [:s | s
			call: #main;
			prim: #pop];
		script: #toggle
		ticking: false
		delay: 0
		instructions: [:s | s
			registerArgument: #a;
			registerArgument: #b;
			push: #a;
			push: #b;
			prim: #subtract;
			write: 13;
			prim: #ret;
			turnOn: 11];
		script: #main 
		ticking: false 
		delay: 10000000
		instructions: [:s | s
			push: 1;
			read: 13;
			call: #toggle;
			write: 11]].
	self writeFile: 'Test023ScriptCallWithTwoParametersWithoutReturnValueWithExplicitReturn'
		program: program