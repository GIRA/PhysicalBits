contents
acceptContents: aText.
	super acceptContents: aText.
	self controlPanel
		ifNil: [UIManager default inform: 'No control panel found!']
		ifNotNil: [:p | p run: aText asString].
	^ false "Keep it always dirty so that we don't need to make a change to recompile"