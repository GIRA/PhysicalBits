initialize
initialize
	super initialize.
	"It doesn't matter what the argument was. The END bytecode 
	always has 16rF as argument"
	argument := 16rF