tests
test041PrimitiveCoroutine
	| program script |
	program := self compile: 'func c() { return coroutine(); }'.
	script := program scripts detect: [:s | s name = #c].
	self assert: script instructions = {
		prim arg: (UziPrimitive named: #coroutine).
		prim arg: (UziPrimitive named: #retv)
	}
