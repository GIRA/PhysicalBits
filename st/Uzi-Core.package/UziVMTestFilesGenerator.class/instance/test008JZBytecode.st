tests
test008JZBytecode
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true 
		delay: 0
		instructions: [:s | s
			read: 13;
			jz: #zero;
			turnOff: 13;
			jmp: #end;
			label: #zero;
			turnOn: 13;
			label: #end]].
	self writeFile: 'Test008JZBytecode'
		program: program