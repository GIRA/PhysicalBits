reporting
sendPinsReporting
	"
	Sends to the arduino the reporting pins. This is called when connecting to
	an arduino.
	"
	pinsReporting do: [:pinNumber |
		self setPin: pinNumber report: true].