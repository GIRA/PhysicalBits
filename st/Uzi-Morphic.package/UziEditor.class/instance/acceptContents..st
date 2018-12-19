contents
acceptContents: aText.
	super acceptContents: aText.
	self protocol
		ifNil: [UIManager default inform: 'No protocol found!']
		ifNotNil: [:p | p run: aText asString].
	^ false "Keep it always dirty so that we don't need to make a change to recompile"