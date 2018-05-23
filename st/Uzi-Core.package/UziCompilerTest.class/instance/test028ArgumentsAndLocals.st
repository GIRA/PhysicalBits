tests
test028ArgumentsAndLocals
	| program script |
	program := self compile: 'func mul(a) { var b; b = 2; return a * b; }'.
	script := program scripts first.
	self assert: #(a) equals: (script arguments collect: [:each | each name upTo: $#]).
	self assert: #(b) equals: (script locals collect: [:each | each name upTo: $#]).
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 2]).
		writeLocal arg: (script locals detect: [:var | (var name upTo: $#) = #b]).
		readLocal arg: (script arguments detect: [:var | (var name upTo: $#) = #a]).
		readLocal arg: (script locals detect: [:var | (var name upTo: $#) = #b]).
		prim arg: (UziPrimitive named: #multiply).
		prim arg: (UziPrimitive named: #retv)
	}
