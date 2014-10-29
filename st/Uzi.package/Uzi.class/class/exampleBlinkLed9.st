script examples
exampleBlinkLed9
	^ Uzi
		script: #blinkLed9
		ticking: true
		delay: 1000
		bytecodes: [:uzi | uzi
			pushLit: 9;
			primGetValue;
			jnz: 4;
			pushLit: 9;
			pushLit: 1;
			primSetValue;
			jmp: 3;
			pushLit: 9;
			pushLit: 0;
			primSetValue;
			end].
	