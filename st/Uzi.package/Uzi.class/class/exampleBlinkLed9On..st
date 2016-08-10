examples
exampleBlinkLed9On: programBuilder
	^ programBuilder
		script: #blinkLed9
		ticking: true
		delay: 1000
		bytecodes: [:u2 | u2
			push: 9;
			prim: #getValue;
			jnz: 4;
			push: 9;
			push: 1;
			prim: #setValue;
			jmp: 3;
			push: 9;
			push: 0;			
			prim: #setValue;
			end]