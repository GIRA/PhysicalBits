tests
testConditionals4
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '#main ticking [D13 read ifFalse: [D13 on]]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program variables detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jnz arg: 2.
		push arg: (program variables detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #turnOn).
	}
