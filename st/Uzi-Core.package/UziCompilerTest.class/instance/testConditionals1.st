tests
testConditionals1
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [D13 read ifTrue: [D13 off] ifFalse: [D13 on]]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program variables detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jz arg: 3.
		push arg: (program variables detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #turnOff).
		jmp arg: 2.
		push arg: (program variables detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #turnOn).
	}
