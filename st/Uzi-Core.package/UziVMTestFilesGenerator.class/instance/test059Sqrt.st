tests
test059Sqrt
	| program |	
	program := '
	task main() running 1/s {
		write(D9, sqrt(0.25));
	}'.
	self writeFile: thisContext method selector
		program: program