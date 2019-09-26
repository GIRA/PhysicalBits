tests
test066Asin
	| program |	
	program := '
	task main() running 1/s {
		write(D7, asin(0)); "0"
		write(D8, asin(0.48)); "0.500654712404588"
		write(D9, isCloseTo(asin(-1), -1.570796326794897)); "1"
	}'.
	self writeFile: thisContext method selector
		program: program