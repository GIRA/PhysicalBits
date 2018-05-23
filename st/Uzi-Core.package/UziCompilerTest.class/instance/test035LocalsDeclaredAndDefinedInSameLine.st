tests
test035LocalsDeclaredAndDefinedInSameLine
	| program script |
	program := self compile: 'func mul(a) { var b = 2; return a * b; }'.
	script := program scripts first.
	self assert: #(a) equals: (script arguments collect: [:var | var name upTo: $#]).
	self assert: #(b) equals: (script locals collect: [:var | var name upTo: $#]).
	self assert: #(2) equals: (script locals collect: [:each | each value]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | (var name upTo: $#) = #a]).
		readLocal arg: (script locals detect: [:var | (var name upTo: $#) = #b]).
		prim arg: (UziPrimitive named: #multiply).
		prim arg: (UziPrimitive named: #retv)
	}
