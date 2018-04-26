tests
test043ForLoop
	| program |
	program := Uzi compileString: 'task for() running { for i = 7 to 11 { turnOn(i); }}'.
	self writeFile: 'Test043ForLoop'
		contents: (UziProtocol new run: program)