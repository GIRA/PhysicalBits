tests
test079IsWhole
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D7, isWhole(9.5));
		if isWhole(0) && isWhole(100000) { write(D8, 0.5); }
		write(D9, isWhole(-10));
	}'.
	self writeFile: thisContext method selector capitalized
		program: program