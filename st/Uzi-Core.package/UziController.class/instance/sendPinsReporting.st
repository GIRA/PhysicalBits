reporting
sendPinsReporting
	"
	Sends to the arduino the reporting pins. This is called when connecting to
	an arduino.
	"
	self pinsReporting do: [:pinNumber |
		self setPin: pinNumber report: true].