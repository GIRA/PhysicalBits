tests
test067Acos
	| program |	
	program := Uzi compileString: '
	task main() running 1/s {
		write(D7, acos(1)); "0"
		write(D8, acos(0.8777777777)); "0.4995926612045067"
		write(D9, isCloseTo(acos(0), 1.570796326794897)); "1"
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)