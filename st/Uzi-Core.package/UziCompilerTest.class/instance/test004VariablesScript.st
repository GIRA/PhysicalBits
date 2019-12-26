tests
test004VariablesScript
	| program script1 |
	program := self compile: 'var a; var b; task main() running { write(a, b); }'.
	"We have 3 globals because the task interval (0) is also a global"
	self assert: 3 equals: program globals size.
	script1 := program scripts detect: [:s | s name = #main].
	self assert: script1 instructions = {
		push arg: (program globals detect: [:var | var name = 'a']).
		push arg: (program globals detect: [:var | var name = 'b']).
		prim arg: (UziPrimitive named: #write).
	}
