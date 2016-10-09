initialize-release
initialize
	super initialize.
	protocol := UziProtocol new.
	self color: Color white;
		borderWidth: 2;
		borderColor: Color red;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #topToBottom;
		buildSubmorphs