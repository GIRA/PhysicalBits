tests
test006ProgramWithJumpsCountsInstructionsCorrectly
	| encoded expected program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true 
		delay: 500
		instructions: [:s | s
			read: 13;
			jnz: 2;
			turnOn: 13;
			jmp: 1;
			turnOff: 13]].
	encoded := UziEncoder new encode: program.
	expected :=  #[
		1 "Script count"
		1 "Globals count"
		5 16r01 16rF4
		192 3 "Script header (running 2/s, no args, no locals)"
		5 "Instruction count"
		109 "read: 13"
		242 2 "jnz: 2"
		13 "turnOn: 13"
		240 1 "jmp: 1"
		45 "turnOff: 13"
	].
	self assert: expected equals: encoded