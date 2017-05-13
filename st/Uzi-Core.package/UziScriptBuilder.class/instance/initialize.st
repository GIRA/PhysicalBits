initialization
initialize 
	super initialize.
	labels := Dictionary new.
	instructions := OrderedCollection new.
	args := OrderedCollection new.
	locals := OrderedCollection new