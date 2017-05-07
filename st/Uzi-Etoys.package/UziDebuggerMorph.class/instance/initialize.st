initialization
initialize
	super initialize.
	self color: Color transparent;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #leftToRight;
		buildSubmorphs;
		break