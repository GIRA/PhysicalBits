tests
testBlinkLed9Script
	| cc program script1 |
	cc := UziCompiler new.
	program := cc compile: '@blinkLed9 ticking 1/s [
		D9 read
			ifTrue: [D9 off]
			ifFalse: [D9 on]
	]'.
	self assert: 1 equals: program scripts size.
	script1 := program scripts first.
	self assert: script1 name = #blinkLed9.
	self assert: script1 isTicking.
	self assert: script1 delay = 1000.
	self assert: #(9) equals: (program variables collect: #value).
	self assert: script1 instructions = {
		push arg: program variables first.
		prim arg: (UziPrimitive named: #read).
		jz arg: 3.
		push arg: program variables first.
		prim arg: (UziPrimitive named: #turnOff).
		jmp arg: 2.		
		push arg: program variables first.
		prim arg: (UziPrimitive named: #turnOn).
	}
