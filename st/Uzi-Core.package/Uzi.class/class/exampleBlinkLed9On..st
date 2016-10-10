examples
exampleBlinkLed9On: programBuilder
	^ programBuilder
		script: #blinkLed9
		ticking: true
		delay: 1000
		instructions: [:u2 | u2
			push: 9;
			prim: #read;
			jnz: 4;
			push: 9;
			push: 1;
			prim: #write;
			jmp: 3;
			push: 9;
			push: 0;			
			prim: #write]