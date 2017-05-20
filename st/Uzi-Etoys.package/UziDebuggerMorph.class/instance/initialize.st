initialization
initialize
	super initialize.
	printer := UziProgramPrinter new.
	self color: Color transparent;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #leftToRight;
		buildSubmorphs;
		break