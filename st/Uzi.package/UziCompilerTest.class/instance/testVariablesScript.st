tests
testVariablesScript
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '#main ticking [ a write: b ]'.
	self assert: 2 equals: program variables size.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 instructions = {
		pushLit arg: (program variables detect: [:var | var name = 'a']).
		pushLit arg: (program variables detect: [:var | var name = 'b']).
		primCall arg: (prim named: #write).
	}
