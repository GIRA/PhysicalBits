reporting
sendPinsReporting
	"
	Sends to the arduino the reporting pins. This is called when connecting to
	an arduino.
	"
	self pinsReporting do: [:pinName |
		self setPin: pinName report: true].