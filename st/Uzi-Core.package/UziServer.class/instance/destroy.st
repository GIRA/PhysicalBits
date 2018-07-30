initialize-release
destroy
	super destroy.
	self at: #uzi
		ifPresent: #destroy
		ifAbsent: nil