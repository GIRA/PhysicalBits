tests
test060Abs
	| program |	
	program := '
	task main() running 1/s {
		write(D13, abs(-1));
		write(D9, abs(-0.5));
		write(D10, abs(0.5));
	}'.
	self writeFile: thisContext method selector
		program: program