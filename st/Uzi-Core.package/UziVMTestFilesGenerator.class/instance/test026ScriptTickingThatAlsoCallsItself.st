tests
test026ScriptTickingThatAlsoCallsItself
	| program |
	program := Uzi program: [:p | p
		registerGlobal: #result value: 0;
		registerGlobal: #counter value: 0;
		script: #main
		ticking: true
		delay: 1000
		instructions: [:s | s
			registerArgument: #call value: 0;
			push: #call;
			jz: 15;
			push: #call;
			push: 1;
			prim: #equals;
			jz: 3;
			push: 2;
			prim: #retv;
			jmp: 7;
			push: 2;
			push: #call;
			push: 1;
			prim: #subtract;
			call: #main;
			prim: #multiply;
			prim: #retv;
			jmp: 2;
			push: 13;
			prim: #toggle];
		script: #button
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 9;
			prim: #read;
			jz: -3;
			push: 9;
			prim: #read;
			jnz: -3;
			push: #counter;
			push: 1;
			prim: #add;
			pop: #counter;
			push: #counter;
			call: #main;
			push: 256;
			prim: #divide;
			pop: #result;
			push: 11;
			push: #result;
			prim: #write]].
	self writeFile: 'Test026ScriptTickingThatAlsoCallsItself'
		contents: (UziProtocol new run: program)