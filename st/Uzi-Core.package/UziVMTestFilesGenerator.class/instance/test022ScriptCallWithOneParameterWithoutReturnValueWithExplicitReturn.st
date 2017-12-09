tests
test022ScriptCallWithOneParameterWithoutReturnValueWithExplicitReturn
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
			registerArgument: #n;
			push: 1;
			push: #n;
			prim: #subtract;
			write: 13;
			prim: #ret;
			turnOn: 11];
		script: #main 
		ticking: false 
		delay: 10000000
		instructions: [:s | s
			read: 13;
			call: #toggle;
			write: 11]].
	self writeFile: 'Test022ScriptCallWithOneParameterWithoutReturnValueWithExplicitReturn'
		contents: (UziProtocol new run: program)