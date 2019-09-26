tests
test078IsPrime
	| program |	
	program := '
	task main() running 1/s {
		write(D7, isPrime(9));
		if isPrime(2) && isPrime(3) { write(D8, 0.5); }
		write(D9, isPrime(5));
	}'.
	self writeFile: thisContext method selector
		program: program