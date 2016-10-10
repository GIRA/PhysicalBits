initialize
initialize
	super initialize.
	key := self externalName.
	value := 0.
	self color: Color transparent;
		listDirection: #leftToRight;
		buildSubmorphs