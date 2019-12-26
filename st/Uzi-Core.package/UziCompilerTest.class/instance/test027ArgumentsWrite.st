tests
test027ArgumentsWrite
	| program script |
	program := self compile: 'proc mul(a, b) { b = a * b; }'.
	script := program scripts last.
	self assert: #(a b) equals: (script arguments collect: [:var | var name copyUpTo: $#]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | (var name copyUpTo: $#) = #a]).
		readLocal arg: (script arguments detect: [:var | (var name copyUpTo: $#) = #b]).
		prim arg: (UziPrimitive named: #multiply).
		writeLocal arg: (script arguments detect: [:var | (var name copyUpTo: $#) = #b])
	}
