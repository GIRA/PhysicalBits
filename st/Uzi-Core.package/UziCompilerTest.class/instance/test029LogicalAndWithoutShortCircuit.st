tests
test029LogicalAndWithoutShortCircuit
	| program script |
	program := self compile: 'func foo() { return 1 && 0;}'.
	script := program scripts last.
	self assert: script instructions = {
		push arg: (program constants detect: [:var | var value = 1]).
		push arg: (program constants detect: [:var | var value = 0]).
		prim arg: (UziPrimitive named: #logicalAnd).
		prim arg: (UziPrimitive named: #retv)
	}
