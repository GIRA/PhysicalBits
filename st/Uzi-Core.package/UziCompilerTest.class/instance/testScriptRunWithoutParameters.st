tests
testScriptRunWithoutParameters
	| cc program script |
	cc := UziCompiler new.
	program := cc compile: '@main [@foo value]. @foo [D13 toggle]'.
	script := program scripts first.
	self assert: script instructions = {
		call arg: #foo.
		prim arg: (UziPrimitive named: #pop)
	}
