building
rebuild 
	self submorphsDo: [:m | m delete].
	self buildSubmorphs 