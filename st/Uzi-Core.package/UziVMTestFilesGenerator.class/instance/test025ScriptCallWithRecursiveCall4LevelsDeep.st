tests
test025ScriptCallWithRecursiveCall4LevelsDeep
	| program |
	program := Uzi program: [:p | p
		registerGlobal: #c;
		script: #foo
		ticking: false
		delay: 0 
		instructions: [:s | s
			registerArgument: #n;
			push: #n;
			push: 0;
			prim: #equals;
			jz: #true;
			push: 42;
			prim: #retv;
			jmp: #end;
			label: #true;
			push: #n;
			push: 1;
			prim: #subtract;
			call: #foo;
			prim: #retv;
			label: #end];
		script: #main
		ticking: true
		delay: 1000
		instructions: [:s | s
			push: 4;
			call: #foo;
			pop: #c;
			push: #c;
			write: 13]].
	self writeFile: 'Test025ScriptCallWithRecursiveCall4LevelsDeep'
		program: program