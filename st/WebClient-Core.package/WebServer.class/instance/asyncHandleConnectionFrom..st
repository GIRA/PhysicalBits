handling
asyncHandleConnectionFrom: aSocket
	"Handle an incoming connection asynchronously to avoid blocking the listener."

	| handlerProcess |
	handlerProcess := [[
		[self handleConnectionFrom: aSocket] on: Error do:[:ex| aSocket destroy].
		mutex critical:[connections remove: Processor activeProcess ifAbsent:[]].
	] ensure:[aSocket destroy]] newProcess.

	mutex critical:[connections add: handlerProcess].

	handlerProcess resume.
