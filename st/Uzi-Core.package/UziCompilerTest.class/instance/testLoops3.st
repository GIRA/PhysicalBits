tests
testLoops3
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [[D13 read] whileTrue]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jnz arg: -3.
	}
