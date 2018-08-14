reporting
sendPinsReporting
	"
	Sends to the arduino the reporting pins. This is called when connecting to
	an arduino.
	"
	self pins do: [:pin |
		self setPin: pin number report: true].