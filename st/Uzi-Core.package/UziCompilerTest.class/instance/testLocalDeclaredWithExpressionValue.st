tests
testLocalDeclaredWithExpressionValue
	| program script |
	program := cc compile: 'func mul(a) { var b = a * 2; return b; }'.
	script := program scripts first.
	self assert: #(a) equals: (script arguments collect: [:each | each name]).
	self assert: #(b) equals: (script locals collect: [:each | each name]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | var name = #a]).
		push arg: (program globals detect: [:var | var value = 2]).
		prim arg: (UziPrimitive named: #multiply).
		writeLocal arg: (script locals detect: [:var | var name = #b]).
		readLocal arg: (script locals detect: [:var | var name = #b]).
		prim arg: (UziPrimitive named: #retv)
	}
