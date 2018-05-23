tests
test017Arguments
	| program script |
	program := self compile: 'func mul(a, b) { return a * b; }'.
	script := program scripts first.
	self assert: #(a b) equals: (script arguments collect: [:each | each name upTo: $#]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | (var name upTo: $#) = #a]).
		readLocal arg: (script arguments detect: [:var | (var name upTo: $#) = #b]).
		prim arg: (UziPrimitive named: #multiply).
		prim arg: (UziPrimitive named: #retv)
	}
