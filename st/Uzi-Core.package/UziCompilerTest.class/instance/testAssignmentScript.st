tests
testAssignmentScript
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [ c := 1 ]'.
	self assert: 2 equals: program variables size.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 instructions = {
		push arg: (program variables detect: [:var | var value = 1]).
		pop arg: (program variables detect: [:var | var name = 'c']).
	}
