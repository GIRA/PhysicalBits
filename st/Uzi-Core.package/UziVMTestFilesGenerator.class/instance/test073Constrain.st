tests
test073Constrain
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D7, constrain(-10, -3, 5) == -3);
		write(D8, constrain(0.5, -3, 5));
		write(D9, constrain(10, -3, 5) == 5);
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)