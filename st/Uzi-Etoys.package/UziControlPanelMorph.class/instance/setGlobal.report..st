accessing
setGlobal: globalName report: aBoolean
	[protocol setGlobal: globalName report: aBoolean.
	self rebuild]
		on: Error do: ["Nothing"]