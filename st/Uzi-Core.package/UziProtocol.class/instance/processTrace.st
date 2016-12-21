receiving
processTrace
	| size text |
	size := serial next.
	text := String streamContents: [:stream |
		size timesRepeat: [stream nextPut: (Character value: serial next)]].
	WorldState 
		addDeferredUIMessage: [Transcript 
			show: 'TRACE: ';
			show: text;
			cr]