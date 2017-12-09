tests
test031PrimitiveLogicalOr
	| program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 1000
		instructions: [:s | s
			push: 1;
			push: 0;
			prim: #logicalOr;
			jz: 3;
			push: 13;
			prim: #toggle;
			jmp: 2;
			push: 11;
			prim: #toggle]].
	self writeFile: 'Test031PrimitiveLogicalOr'
		contents: (UziProtocol new run: program)