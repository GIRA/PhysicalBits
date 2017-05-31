tests
testLogicalAndWithShortCircuit
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '
	@foo [^ 1 && (@bar value)]
	@bar [^ 42]'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program constants detect: [:var | var value = 1]).
		jz arg: 2.
		call arg: #bar.
		jmp arg: 1.
		push arg: (program constants detect: [:var | var value = 0]).
		prim arg: (UziPrimitive named: #retv)
	}
