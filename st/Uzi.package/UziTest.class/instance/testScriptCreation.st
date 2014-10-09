tests
testScriptCreation
	| script bytecodes |
	script := Uzi
		script: #main
		ticking: true
		delay: 1000
		bytecodes: [:uzi | uzi
			pushLit: 9;
			pushLit: 10;
			primCall: #getValue;
			primCall: #setValue;
			end].
	self assert: script name = #main.
	self assert: script isTicking.
	self assert: script delay = 1000.
	self assert: (script literals collect: #value) = #(9 10).
	self assert: script variables isEmpty.
	bytecodes := script bytecodes.
	self assert: bytecodes size = 5.
	self assert: bytecodes = {
		pushLit arg: script literals first.
		pushLit arg: script literals second.
		primCall arg: (prim named: #getValue).
		primCall arg: (prim named: #setValue).
		end.
	}
