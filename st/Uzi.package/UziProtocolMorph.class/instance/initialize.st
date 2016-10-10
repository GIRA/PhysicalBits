initialize-release
initialize
	super initialize.
	protocol := UziProtocol new.
	pinTexts := Dictionary new.
	globalTexts := Dictionary new.
	self color: Color white;
		borderWidth: 2;
		borderColor: Color red;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #topToBottom;
		buildSubmorphs