tests
test069Power
	| program |	
	program := '
	task main() running 1/s {
		write(D7, 0**1); "0"
		write(D8, 0.7071067811865475**2); "0.5"
		write(D9, 1**0); "1"
	}'.
	self writeFile: thisContext method selector
		program: program