tests
test081IsNegative
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D7, isNegative(0));
		if isNegative(-10) && isNegative(-0.54534) { write(D8, 0.5); }
		write(D9, isNegative(-1));
	}'.
	self writeFile: thisContext method selector capitalized
		program: program