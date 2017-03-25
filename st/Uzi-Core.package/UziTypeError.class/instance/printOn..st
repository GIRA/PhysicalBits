printing
printOn: stream
	super printOn: stream.
	stream cr; tab.
	stream 
		nextPutAll: '... ';
		nextPutAll: self token inputValue;
		nextPutAll: ' ...'