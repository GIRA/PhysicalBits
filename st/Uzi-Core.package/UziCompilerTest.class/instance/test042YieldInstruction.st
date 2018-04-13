tests
test042YieldInstruction
	| program script |
	program := self compile: 'func c() { yield; }'.
	script := program scripts first.
	self assert: script instructions = {
		prim arg: (UziPrimitive named: #yield)
	}
