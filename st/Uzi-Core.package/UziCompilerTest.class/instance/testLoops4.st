tests
testLoops4
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [[D13 read] whileFalse]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jz arg: -3.
	}
