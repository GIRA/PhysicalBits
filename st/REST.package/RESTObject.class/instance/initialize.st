initialize-release
initialize 
	super initialize.	
	self onGET: [:req | self processGET: req]