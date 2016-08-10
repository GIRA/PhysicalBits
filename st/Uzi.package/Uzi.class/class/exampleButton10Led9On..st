examples
exampleButton10Led9On: programBuilder
	^ programBuilder
		script: #button10Led9
		ticking: true
		delay: 0
		bytecodes: [:uzi | uzi
			push: 9;
			push: 10;
			prim: #getValue;
			prim: #setValue;
			end]