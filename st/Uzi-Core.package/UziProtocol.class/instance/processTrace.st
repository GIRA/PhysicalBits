receiving
processTrace
	| size text |
	size := serial next.
	text := String streamContents: [:stream |
		size timesRepeat: [stream nextPut: (Character value: serial next)]].
	UziTranscript log: 'TRACE: {1}' format: { text }; flush