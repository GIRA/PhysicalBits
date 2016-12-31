contents
acceptContents: aText.
	super acceptContents: aText.
	protocol run: aText asString.
	^ false "Keep it always dirty so that we don't need to make a change to recompile"