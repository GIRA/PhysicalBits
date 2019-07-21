accessing
availablePins
	^ pinsByNumber keys sorted collect: [:number | self getPinName: number]