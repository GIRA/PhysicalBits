tests
testSpecialVariables
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [a := #time]'.
	script1 := program scripts first.
	self assert: script1 instructions = {
		prim arg: (UziPrimitive named: #time).
		pop arg: (program variables detect: [:var | var name = 'a']).
	}
