script examples
exampleButton10Led9
	^ self
		script: #button10Led9
		ticking: true
		delay: 0
		bytecodes: [:uzi | uzi
			pushLit: 9;
			pushLit: 10;
			primCall: #getValue;
			primCall: #setValue;
			end].
	