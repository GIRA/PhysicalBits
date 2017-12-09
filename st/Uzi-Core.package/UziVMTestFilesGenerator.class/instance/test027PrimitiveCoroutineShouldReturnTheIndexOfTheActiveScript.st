tests
test027PrimitiveCoroutineShouldReturnTheIndexOfTheActiveScript
	| program |
	program := Uzi program: [:p | p
		script: #foo
		ticking: true
		delay: 0
		instructions: [:s | s
			call: #writeCoroutine;
			prim: #pop;
			push: 1000;
			prim: #yieldTime;
			start: #bar;
			stop: #foo];
		script: #bar
		ticking: false
		delay: 0
		instructions: [:s | s
			call: #writeCoroutine;
			prim: #pop;
			push: 1000;
			prim: #yieldTime;
			start: #foo;
			stop: #bar];
		script: #writeCoroutine
		ticking: false
		delay: 0
		instructions: [:s | s
			push: 11;
			prim: #coroutine;
			prim: #write]].
	self writeFile: 'Test027PrimitiveCoroutineShouldReturnTheIndexOfTheActiveScript'
		contents: (UziProtocol new run: program)