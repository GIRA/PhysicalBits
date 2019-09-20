accessing
taskWatchers
	^ self scriptWatchers select: [:each | each isTask]