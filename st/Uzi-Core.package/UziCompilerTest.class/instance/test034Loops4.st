tests
test034Loops4
	| program script |
	program := self compile: 'task main() running {
		until read (D13);
	}'.
	script := program scripts first.
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jz arg: -3.
	}
