tests
testScriptCreation
	| script bytecodes |
	script := Uzi scriptNamed: #main 
		state: #ticking delay: 10
		literals: #(9 10)	variables: #()
		bytecodes: [:uzi | uzi
			pushLit: 0;
			pushLit: 1;
			primCall: 0;
			primCall: 1;
			end	].
	self assert: script name = #main.
	self assert: script state = #ticking.
	self assert: script delay = 10.
	self assert: script literals = #(9 10).
	self assert: script variables isEmpty.
	bytecodes := script bytecodes.
	self assert: bytecodes size = 5.
	self assert: bytecodes = {
		pushLit arg: 0.
		pushLit arg: 1.
		primCall arg: 0.
		primCall arg: 1.
		end.
	}
