initialization
initialize
	super initialize.
	pc := 1.
	self color: Color transparent;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #leftToRight;
		buildSubmorphs