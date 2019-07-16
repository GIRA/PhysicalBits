accessing
scriptWatchers
	^ self scripts 
		collect: [:script | scriptWatchers at: script ifAbsent: [nil]]
		thenSelect: [:w | w notNil]