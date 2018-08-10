accessing
activateReportingFor: pins
	(pins copyWithoutAll: self protocol pinsReporting)
		do: [:pin | self protocol setPin: pin report: true]