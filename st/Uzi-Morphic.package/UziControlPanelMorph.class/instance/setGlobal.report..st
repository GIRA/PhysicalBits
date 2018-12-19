accessing
setGlobal: globalName report: reportEnabled
	reportEnabled
		ifTrue: [self globalsReporting add: globalName]
		ifFalse: [self globalsReporting remove: globalName ifAbsent: ["Do nothing"]].
	self rebuild