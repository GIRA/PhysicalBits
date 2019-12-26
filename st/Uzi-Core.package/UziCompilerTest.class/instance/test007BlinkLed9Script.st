tests
test007BlinkLed9Script
	| program script1 |
	program := self compile: '
	task blinkLed9() running 1/s {
		if read(D9) {
			turnOff(D9);
		} else {
			turnOn(D9);
		}
	}'.
	script1 := program scripts last.
	self assert: script1 name = #blinkLed9.
	self assert: script1 isTicking.
	self assert: script1 delay value = 1000.
	self assert: #(0 9 1000) equals: (program sortedGlobals collect: #value).
	self assert: script1 instructions = {
		push arg: (program globals detect: [:v | v value = 9]).
		prim arg: (UziPrimitive named: #read).
		jz arg: 3.
		push arg: (program globals detect: [:v | v value = 9]).
		prim arg: (UziPrimitive named: #turnOff).
		jmp arg: 2.		
		push arg: (program globals detect: [:v | v value = 9]).
		prim arg: (UziPrimitive named: #turnOn).
	}
