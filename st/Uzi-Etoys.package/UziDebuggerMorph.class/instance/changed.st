stepping
changed
	super changed.
	self changed: #stackFrames;
		changed: #sourceCodeSelection;
		changed: #textualFormSelection;
		changed: #locals;
		changed: #stackString;
		rebuild