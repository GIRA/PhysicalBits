tests
test014PrimitiveYieldTime
	| program |
	program := Uzi program: [:p | p
		script: #yieldTest
		ticking: true
		delay: 100
		instructions: [:s | s
			turnOn: 13;
			push: 1000;
			prim: #delayMs;
			turnOff: 13]].
	self writeFile: 'Test014PrimitiveYieldTime'
		contents: (UziProtocol new run: program)