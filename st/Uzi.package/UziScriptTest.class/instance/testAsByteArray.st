as yet unclassified
testAsByteArray
	| script |
	script := Uzi scriptNamed: #main
		state: #ticking
		delay: 10 
		literals: #(9 10)
		variables: #()
		bytecodes: [:uzi | uzi
			pushLit: 0;
			pushLit: 1;
			primCall: 0;
			primCall: 1;
			end].
	self assert: script asByteArray = #[
		"Stepping and steptime (true, 10)"
		16r80 16r00 16r00 16r0A
		
		"Literals (9, 10)" 
		16r02 16r08 16r09 16r0A
		
		"Variables"
		16r00
		
		"Bytecodes"
		16r00 16r01 16r50 16r51 16rFF]