tests
test080IsPositive
	| program |	
	program := '
	task main() running 1/s {
		write(D7, isPositive(-10));
		if isPositive(10) && isPositive(100000) { write(D8, 0.5); }
		write(D9, isPositive(0));
	}'.
	self writeFile: thisContext method selector
		program: program