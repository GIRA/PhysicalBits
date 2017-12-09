tests
test016ScriptCallWithoutParametersOrReturnValue
	| program |
	program := Uzi program: [:p | p
		script: #toggle
		ticking: false
		delay: 0
		instructions: [:s | s
			push: 10;
			prim: #toggle;
			prim: #ret;
			turnOn: 13];
		script: #loop
		ticking: true
		delay: 1000
		instructions: [:s | s
			call: #toggle;
			prim: #pop;
			push: 11;
			prim: #toggle]].
	self writeFile: 'Test016ScriptCallWithoutParametersOrReturnValue'
		contents: (UziProtocol new run: program)