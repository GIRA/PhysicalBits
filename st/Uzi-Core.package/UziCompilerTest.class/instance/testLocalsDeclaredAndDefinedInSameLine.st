tests
testLocalsDeclaredAndDefinedInSameLine
	| program script |
	program := cc compile: 'func mul(a) { var b = 2; return a * b; }'.
	script := program scripts first.
	self assert: #(a) equals: (script arguments collect: [:each | each name]).
	self assert: #(b) equals: (script locals collect: [:each | each name]).
	self assert: #(2) equals: (script locals collect: [:each | each value]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | var name = #a]).
		readLocal arg: (script locals detect: [:var | var name = #b]).
		prim arg: (UziPrimitive named: #multiply).
		prim arg: (UziPrimitive named: #retv)
	}
