tests
testSpecialVariables
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [a := #seconds]'.
	script1 := program scripts first.
	self assert: script1 instructions = {
		prim arg: (UziPrimitive named: #seconds).
		pop arg: (program variables detect: [:var | var name = 'a']).
	}
