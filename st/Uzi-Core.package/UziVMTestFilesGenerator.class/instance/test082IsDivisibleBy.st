tests
test082IsDivisibleBy
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D7, isDivisibleBy(9, 5));
		if isDivisibleBy(5, 1) && isDivisibleBy(-15, -3) { write(D8, 0.5); }
		write(D9, isDivisibleBy(64, 8));
	}'.
	self writeFile: thisContext method selector capitalized
		program: program