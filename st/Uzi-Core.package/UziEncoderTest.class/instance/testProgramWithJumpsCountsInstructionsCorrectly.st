as yet unclassified
testProgramWithJumpsCountsInstructionsCorrectly
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
		0 "Globals count"
		128 0 1 244 "Script header (ticking 2 / s)"
		5 "Instruction count"
		109 "read: 13"
		242 2 "jnz: 2"
		13 "turnOn: 13"
		255 1 "jmp: 1"
		45 "turnOff: 13"
	].
	self assert: expected equals: encoded