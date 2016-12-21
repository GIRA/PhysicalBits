initialize-release
initialize
	super initialize.
	rnd := Random new.
	self onGET: [:req | self processGET: req]