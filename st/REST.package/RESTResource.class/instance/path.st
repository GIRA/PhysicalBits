accessing
path
	^ Array streamContents: [:stream | self storePathOn: stream]