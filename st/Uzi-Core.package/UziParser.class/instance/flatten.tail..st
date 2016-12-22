building messages
flatten: msg tail: tail
	^ { msg } , (tail ifNil: [#()])