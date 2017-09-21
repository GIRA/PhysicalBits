tests
test038RepeatConstantLoop
	| program script |
	program := self compile: '
		task main() running { 
			repeat 5 {
				toggle(D13); 
			}
		}'.
	script := program scripts first.
	self assert: 1 equals: script locals size.
	self assert: script instructions = {
		readLocal arg: script locals first.
		push arg: (program globals detect: [:var | var value = 5]).
		prim arg: (UziPrimitive named: #lessThan).
		jz arg: 7.
		push arg: (program globals detect: [:var | var value = 13]).
		prim arg: (UziPrimitive named: #toggle).
		readLocal arg: script locals first.
		push arg: (program globals detect: [:var | var value = 1]).
		prim arg: (UziPrimitive named: #add).
		writeLocal arg: script locals first.
		jmp arg: -11
	}
