accessing
value
	^ self history
		ifEmpty: [nil]
		ifNotEmpty: [:history | history last value]