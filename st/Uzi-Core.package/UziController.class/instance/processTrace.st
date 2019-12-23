receiving
processTrace
	| size text |
	size := serial next.
	text := String streamContents: [:stream |
		size timesRepeat: [stream nextPut: (Character value: serial next)]].
	Uzi logger log: 'TRACE: %1' format: { text }; flush