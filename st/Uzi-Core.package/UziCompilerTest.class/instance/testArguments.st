tests
testArguments
	| program script |
	program := cc compile: 'func mul(a, b) { return a * b; }'.
	script := program scripts first.
	self assert: #(a b) equals: (script arguments collect: [:each | each name]).
	self assert: script instructions = {
		readLocal arg: (script arguments detect: [:var | var name = #a]).
		readLocal arg: (script arguments detect: [:var | var name = #b]).		
		prim arg: (UziPrimitive named: #multiply).
		prim arg: (UziPrimitive named: #retv)
	}
