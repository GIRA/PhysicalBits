reporting
sendGlobalsReporting
	"
	Sends to the arduino the reporting globals. This is called after changing the running program
	"
	self globalsReporting do: [:globalName |
		self setGlobal: globalName report: true].