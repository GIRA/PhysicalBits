tests
test018ScriptRunWithoutParameters
	| program script |
	program := self compile: '
	task main() running { foo(); }
	proc foo() { toggle(D13); }'.
	script := program scripts first.
	self assert: script instructions = {
		call arg: #foo.
		prim arg: (UziPrimitive named: #pop)
	}
