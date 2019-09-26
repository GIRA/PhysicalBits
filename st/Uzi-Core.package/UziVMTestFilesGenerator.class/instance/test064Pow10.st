tests
test064Pow10
	| program |	
	program := '
	task main() running 1/s {
		write(D7, pow10(-3)); "0.001"
		write(D8, pow10(-0.301)); "0.5000345349769785"
		write(D9, pow10(0)); "1"
	}'.
	self writeFile: thisContext method selector
		program: program