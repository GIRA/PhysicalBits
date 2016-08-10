script examples
examplePot15Led9
	^ self
		script: #pot15Led9
		ticking: true
		delay: 0
		bytecodes: [:uzi | uzi
			pushLit: 9;
			pushLit: 15;
			primCall: #getValue;
			primCall: #setValue;
			end].
	