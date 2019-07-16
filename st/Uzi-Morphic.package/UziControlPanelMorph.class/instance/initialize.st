initialize-release
initialize
	super initialize.
	rebuildFlag := false.
	protocol := UziController new.
	{ 
		UziEvents connect.
		UziEvents disconnect.
		UziEvents programUpdate.
	} 
	do: [:evt | protocol when: evt send: #update to: self].
		
	pinTexts := Dictionary new.
	globalTexts := Dictionary new.
	scriptTexts := Dictionary new.
	portName := self class lastPortName.
	self color: Color white;
		borderWidth: 2;
		borderColor: Color red;
		hResizing: #shrinkWrap;
		vResizing: #shrinkWrap;
		listDirection: #topToBottom;		
		buildSubmorphs