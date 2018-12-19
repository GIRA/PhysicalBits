initialize
initialize
	super initialize.
	key := self externalName.
	value := 0.
	valueChanged := [:v | "Do nothing"].
	self color: Color transparent;
		listDirection: #leftToRight;
		cellInset: 5;
		buildSubmorphs