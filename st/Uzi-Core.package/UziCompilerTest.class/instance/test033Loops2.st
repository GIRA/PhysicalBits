tests
test033Loops2
	| program script |
	program := self compile: 'task main() running {
		until read (D13) { turnOn(D11); }
	}'.
	script := program scripts detect: [:s | s name = #main].
	self assert: script instructions = {
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #read).
		jnz arg: 3.
		push arg: (program globals detect: [:var | var value = 11]).
		prim arg: (UziPrimitive named: #turnOn).
		jmp arg: -6
	}