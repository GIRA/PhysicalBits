tests
test037ScriptRunOnce
	| program script |
	program := self compile: 'task main() { toggle(D13); }'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #toggle).
		stop arg: #main
	}
