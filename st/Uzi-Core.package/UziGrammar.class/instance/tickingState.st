grammar-scripts
tickingState
	^ ('ticking' asParser / 'normal' asParser / 'paused' asParser) uziToken