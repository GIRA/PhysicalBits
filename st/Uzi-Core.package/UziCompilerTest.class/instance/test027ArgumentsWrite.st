tests
test027ArgumentsWrite
	| program script |
	program := self compile: 'proc mul(a, b) { b = a * b; }'.
	script := program scripts first.
	self assert: #(a b) equals: (script arguments collect: [:var | var name upTo: $#]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | (var name upTo: $#) = #a]).
		readLocal arg: (script arguments detect: [:var | (var name upTo: $#) = #b]).
		prim arg: (UziPrimitive named: #multiply).
		writeLocal arg: (script arguments detect: [:var | (var name upTo: $#) = #b])
	}
