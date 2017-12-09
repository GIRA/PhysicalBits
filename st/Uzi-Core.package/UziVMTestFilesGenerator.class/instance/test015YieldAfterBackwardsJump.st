tests
test015YieldAfterBackwardsJump
	| program |
	program := Uzi program: [:p | p
		script: #blink 
		ticking: true 
		delay: 1000
		instructions: [:s | s push: 11; prim: #toggle];
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13; prim: #toggle;
			label: #label1; read: 15; jz: #label1;
			label: #label2; read: 15; jnz: #label2]].
	self writeFile: 'Test015YieldAfterBackwardsJump'
		contents: (UziProtocol new run: program)