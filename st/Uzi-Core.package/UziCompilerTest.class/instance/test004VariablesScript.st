tests
test004VariablesScript
	| program script1 |
	program := self compile: 'var a; var b; task main() running { write(a, b); }'.
	self assert: 2 equals: program globals size.
	self assert: program scripts size = 1.
	script1 := program scripts first.
	self assert: script1 instructions = {
		push arg: (program globals detect: [:var | var name = 'a']).
		push arg: (program globals detect: [:var | var name = 'b']).
		prim arg: (UziPrimitive named: #write).
	}
