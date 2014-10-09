class initialization
initialize
	"
	self initialize.
	"
	primitives := Dictionary new
		at: #getValue put: (self code: 0);
		at: #setValue put: (self code: 1);
		yourself 