tests
test061NaturalLogarithm
	| program |	
	program := '
	task main() running 1/s {
		write(D7, ln(1)); "0"
		write(D8, ln(2) - 0.1931471805599453); "0.5"
		write(D9, ln(3)); "1.09"
	}'.
	self writeFile: thisContext method selector
		program: program