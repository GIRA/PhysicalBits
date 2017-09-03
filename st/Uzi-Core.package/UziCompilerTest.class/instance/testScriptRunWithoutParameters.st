tests
testScriptRunWithoutParameters
	| program script |
	program := self compile: '
	task main() { foo(); }
	proc foo() { toggle(D13); }'.
	script := program scripts first.
	self assert: script instructions = {
		call arg: #foo.
		prim arg: (UziPrimitive named: #pop)
	}
