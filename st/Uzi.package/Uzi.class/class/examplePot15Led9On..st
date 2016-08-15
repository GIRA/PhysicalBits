examples
examplePot15Led9On: programBuilder
	^ programBuilder
		script: #pot15Led9
		ticking: true
		delay: 0
		bytecodes: [:uzi | uzi
			push: 9;
			push: 15;
			prim: #getValue;
			prim: #setValue]