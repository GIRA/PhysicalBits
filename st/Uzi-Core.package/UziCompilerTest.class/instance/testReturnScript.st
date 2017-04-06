tests
testReturnScript
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '@main ticking [ ^ 1 ]'.
	script1 := program scripts first.
	self assert: script1 instructions = {
		push arg: (program variables detect: [:var | var value = 1]).
		prim arg: (UziPrimitive named: #retv)
	}
