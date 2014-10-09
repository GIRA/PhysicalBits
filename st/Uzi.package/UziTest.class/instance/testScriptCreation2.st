tests
testScriptCreation2
	| script |
	script := Uzi script: #main 
		ticking: true delay: 1000
		bytecodes: [:uzi | uzi
			pushLit: 9;
			primCall: #getValue;
			jnz: 4;
			pushLit: 9;
			pushLit: 255;
			primCall: #setValue;
			jmp: 3;
			pushLit: 9;
			pushLit: 0;
			primCall: #setValue;
			end	].
	self assert: script name = #main.
	self assert: script isTicking.
	self assert: script delay = 1000.
	self assert: (script literals collect: #value) = #(9 255 0).
	self assert: script variables isEmpty.
	self assert: script bytecodes = {
		pushLit arg: script literals first.
		primCall arg: (prim named: #getValue).
		jnz arg: 4.
		pushLit arg: script literals first.
		pushLit arg: script literals second.
		primCall arg: (prim named: #setValue).
		jmp arg: 3.
		pushLit arg: script literals first.
		pushLit arg: script literals third.
		primCall arg: (prim named: #setValue).
		end	
	}
