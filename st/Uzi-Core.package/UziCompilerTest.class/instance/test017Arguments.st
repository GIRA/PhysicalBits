tests
test017Arguments
	| program script |
	program := self compile: 'func mul(a, b) { return a * b; }'.
	script := program scripts last.
	self assert: #(a b) equals: (script arguments collect: [:each | each name copyUpTo: $#]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | (var name copyUpTo: $#) = #a]).
		readLocal arg: (script arguments detect: [:var | (var name copyUpTo: $#) = #b]).
		prim arg: (UziPrimitive named: #multiply).
		prim arg: (UziPrimitive named: #retv)
	}
