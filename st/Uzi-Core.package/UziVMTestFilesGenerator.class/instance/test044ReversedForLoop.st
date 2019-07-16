tests
test044ReversedForLoop
	| program |
	program := Uzi compileString: 'task for() running { for i = 11 to 7 by -1 { turnOn(i); }}'.
	self writeFile: 'Test044ReversedForLoop'
		program: program