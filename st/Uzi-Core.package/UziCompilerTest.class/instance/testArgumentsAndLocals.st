tests
testArgumentsAndLocals
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@mul [ :a || b | b := 2. ^ a * b ]'.
	script := program scripts first.
	self assert: #(a b) equals: (script locals collect: [:each | each name]).
	self assert: script instructions = {
		push arg: (program variables detect: [:var | var value = 2]).
		writeLocal arg: (script locals detect: [:var | var name = #b]).
		readLocal arg: (script locals detect: [:var | var name = #a]).
		readLocal arg: (script locals detect: [:var | var name = #b]).
		prim arg: (UziPrimitive named: #multiply).
		prim arg: (UziPrimitive named: #retv)
	}
