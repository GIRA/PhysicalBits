tests
test029PrimitiveBitwiseOr
	| program |
	program := Uzi program: [:p | p
		registerGlobal: #n value: 0;
		script: #main
		ticking: true
		delay: 1000
		instructions: [:s | s
			push: 11;
			push: #n;
			push: 1;
			prim: #bitwiseOr;
			push: 100;
			prim: #divide;
			prim: #write;
			push: #n;
			push: 1;
			prim: #add;
			pop: #n]].
	self writeFile: 'Test029PrimitiveBitwiseOr'
		contents: (UziProtocol new run: program)