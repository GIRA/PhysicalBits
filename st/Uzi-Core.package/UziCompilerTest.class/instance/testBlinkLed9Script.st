tests
testBlinkLed9Script
	| program script1 |
	program := self compile: '
	task blinkLed9() running 1/s {
		if read(D9) {
			turnOff(D9);
		} else {
			turnOn(D9);
		}
	}'.
	self assert: 1 equals: program scripts size.
	script1 := program scripts first.
	self assert: script1 name = #blinkLed9.
	self assert: script1 isTicking.
	self assert: script1 delay = 1000.
	self assert: #(9) equals: (program globals collect: #value).
	self assert: script1 instructions = {
		push arg: program globals first.
		prim arg: (UziPrimitive named: #read).
		jz arg: 3.
		push arg: program globals first.
		prim arg: (UziPrimitive named: #turnOff).
		jmp arg: 2.		
		push arg: program globals first.
		prim arg: (UziPrimitive named: #turnOn).
	}
