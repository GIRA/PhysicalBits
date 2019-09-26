tests
test068Atan
	| program |	
	program := '
	task main() running 1/s {
		write(D7, atan(0)); "0"
		write(D8, atan(0.5464)); "0.5000750944782615"
		write(D9, atan(1.58)); "1.006528137936965"
	}'.
	self writeFile: thisContext method selector
		program: program