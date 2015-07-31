accessing
next: size
	^ Array streamContents: [:stream |
		size timesRepeat: [stream nextPut: self next]]