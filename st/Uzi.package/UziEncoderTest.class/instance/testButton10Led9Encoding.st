as yet unclassified
testButton10Led9Encoding
	| program encoder |
	encoder := UziEncoder new.
	program := Uzi program: [:p | p
			script: #main
			ticking: true
			delay: 10
			bytecodes: [:uzi | uzi
				push: 9;
				push: 10;
				prim: #getValue;
				prim: #setValue;
				end]].
	self assert: (encoder encode: program) = #[
		"How many scripts"
		1
		
		"Variables (9, 10)" 
		16r02 16r08 16r09 16r0A
		
		"Script 1:
		======"
		"Stepping and steptime (true, 10)"
		16r80 16r00 16r00 16r0A
		
		"Bytecodes"
		16r20 16r21 16r50 16r51 16rFF]