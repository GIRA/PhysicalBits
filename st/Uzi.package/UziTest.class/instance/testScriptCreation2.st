tests
testScriptCreation2
	| script |
	script := Uzi scriptNamed: #main 
		state: #ticking delay: 1000
		literals: #(9 255 0) variables: #()
		bytecodes: [:uzi | uzi
			pushLit: 0;
			primCall: #getValue;
			jnz: 4;
			pushLit: 0;
			pushLit: 1;
			primCall: #setValue;
			jmp: 3;
			pushLit: 0;
			pushLit: 2;
			primCall: #setValue;
			end	].
	self assert: script name = #main.
	self assert: script state = #ticking.
	self assert: script delay = 1000.
	self assert: script literals = #(9 255 0).
	self assert: script variables isEmpty.
	self assert: script bytecodes = {
		pushLit arg: 0.
		primCall arg: 0.
		jnz arg: 4.
		pushLit arg: 0.
		pushLit arg: 1.
		primCall arg: 1.
		jmp arg: 3.
		pushLit arg: 0.
		pushLit arg: 2.
		primCall arg: 1.
		end	
	}
