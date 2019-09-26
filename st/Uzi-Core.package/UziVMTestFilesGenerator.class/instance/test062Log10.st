tests
test062Log10
	| program |	
	program := '
	task main() running 1/s {
		write(D7, log10(1)); "0"
		write(D8, log10(3) + 0.02287874528033756); "0.5"
		write(D9, log10(10)); "1"
	}'.
	self writeFile: thisContext method selector
		program: program