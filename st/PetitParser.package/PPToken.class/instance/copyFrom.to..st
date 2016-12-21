copying
copyFrom: aStartInteger to: aStopInteger
	^ self class on: collection start: start + aStartInteger - 1 stop: stop + aStopInteger - 3 value: value