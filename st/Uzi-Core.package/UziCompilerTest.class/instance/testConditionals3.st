tests
testConditionals3
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [D13 read ifTrue: [D13 off]]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jz arg: 2.
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #turnOff).
	}
