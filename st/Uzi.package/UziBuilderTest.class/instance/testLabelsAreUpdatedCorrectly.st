tests
testLabelsAreUpdatedCorrectly
	| instructions program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true 
		delay: 500
		instructions: [:s | s
			read: 13;
			jnz: #true;
			label: #false; turnOn: 13;
			jmp: #end;
			label: #true; turnOff: 13;
			label: #end]].
	instructions := program scripts first instructions.
	self assert: 2 equals: instructions second argument.
	self assert: 1 equals: instructions fourth argument