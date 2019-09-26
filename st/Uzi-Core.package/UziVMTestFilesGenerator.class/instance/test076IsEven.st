tests
test076IsEven
	| program |	
	program := '
	task main() running 1/s {
		write(D7, isEven(1));
		if isEven(2) && isEven(-2) { write(D8, 0.5); }
		write(D9, isEven(0));
	}'.
	self writeFile: thisContext method selector
		program: program