tests
test036LocalDeclaredWithExpressionValue
	| program script |
	program := self compile: 'func mul(a) { var b = a * 2; return b; }'.
	script := program scripts first.
	self assert: #(a) equals: (script arguments collect: [:var | var name copyUpTo: $#]).
	self assert: #(b) equals: (script locals collect: [:var | var name copyUpTo: $#]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | (var name copyUpTo: $#) = #a]).
		push arg: (program globals detect: [:var | var value = 2]).
		prim arg: (UziPrimitive named: #multiply).
		writeLocal arg: (script locals detect: [:var | (var name copyUpTo: $#) = #b]).
		readLocal arg: (script locals detect: [:var | (var name copyUpTo: $#) = #b]).
		prim arg: (UziPrimitive named: #retv)
	}
