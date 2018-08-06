tests
test077IsOdd
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D7, isOdd(0));
		if isOdd(1) && isOdd(-1) { write(D8, 0.5); }
		write(D9, isOdd(3));
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)