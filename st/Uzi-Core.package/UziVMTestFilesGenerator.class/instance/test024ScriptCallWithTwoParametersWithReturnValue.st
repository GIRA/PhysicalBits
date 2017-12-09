tests
test024ScriptCallWithTwoParametersWithReturnValue
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
			push: #a;
			push: #b;
			prim: #subtract;
			prim: #multiply;
			prim: #retv;
			push: 11;
			prim: #toggle];
		script: #main 
		ticking: false 
		delay: 10000000
		instructions: [:s | s
			push: 1;
			read: 13;
			call: #toggle;
			write: 13]].
	self writeFile: 'Test024ScriptCallWithTwoParametersWithReturnValue'
		contents: (UziProtocol new run: program)