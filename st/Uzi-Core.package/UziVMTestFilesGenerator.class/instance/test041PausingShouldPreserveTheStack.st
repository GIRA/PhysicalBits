tests
test041PausingShouldPreserveTheStack
	| program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			registerLocal: #a value: 5;
			push: #a;
			push: 5;
			prim: #add;
			call: #getPin;
			prim: #toggle];
		script: #getPin
		ticking: false
		delay: 0
		instructions: [:s | s
			registerArgument: #n value: 0;
			registerLocal: #result value: 0;
			push: #n;
			push: 3;
			prim: #add;
			pop: #result;
			pause: #main;
			push: #result;
			prim: #retv];
		script: #awake
		ticking: true
		delay: 1000
		instructions: [:s | s
			resume: #main]].
	self writeFile: 'Test041PausingShouldPreserveTheStack'
		contents: (UziProtocol new run: program)