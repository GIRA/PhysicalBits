as yet unclassified
testButton10Led9Encoding
	| program encoder |
	encoder := UziEncoder new.
	program := UziProgram
		scripts: {Uzi 
			script: #main
			ticking: true
			delay: 10
			bytecodes: [:uzi | uzi
				pushLit: 9;
				pushLit: 10;
				primGetValue;
				primSetValue;
				end]}.
	self assert: (encoder encode: program) = #[
		"How many scripts"
		1
		
		"INPUTS"
		0 2r00100000 0
		
		"OUTPUTS"
		0 2r01000000 0
		
		"Script 1:
		======"
		"Stepping and steptime (true, 10)"
		16r80 16r00 16r00 16r0A
		
		"Literals (9, 10)" 
		16r02 16r08 16r09 16r0A
		
		"Variables"
		16r00
		
		"Bytecodes"
		16r00 16r01 16r50 16r51 16rFF]