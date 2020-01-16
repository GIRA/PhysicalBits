tests
test046ProceduresOverridePrimitives
	| program main toggle |
	program := self compile: '
	task main() running 1/s { toggle(D13); }
	proc toggle(pin) {
		if read(pin) { turnOff(pin); }
		else { turnOn(pin); }
	 }'.
	main := program scripts detect: [:s | s name = #main].
	toggle := program scripts detect: [:s | s name = #toggle].
	self assert: main instructions = {
		push arg: (program globals detect: [:each | each value = 13]).
		call arg: #toggle.		
		prim arg: (UziPrimitive named: #pop)
	}.
	self assert: toggle instructions = {
		readLocal arg: (toggle arguments first).
		prim arg: (UziPrimitive named: #read).
		jz arg: 3.
		readLocal arg: (toggle arguments first).
		prim arg: (UziPrimitive named: #turnOff).
		jmp arg: 2.
		readLocal arg: (toggle arguments first).
		prim arg: (UziPrimitive named: #turnOn).
	}
